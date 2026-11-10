package za.ac.cput.digitalpharmacysystem.service;
/*
 * Sibulele Pama
 * 221447498
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.digitalpharmacysystem.domain.*;
import za.ac.cput.digitalpharmacysystem.dto.PlaceOrderRequest;
import za.ac.cput.digitalpharmacysystem.factory.OrderItemFactory;
import za.ac.cput.digitalpharmacysystem.repository.DeliveryRepository;
import za.ac.cput.digitalpharmacysystem.repository.OrderRepository;
import za.ac.cput.digitalpharmacysystem.util.ActorContext;
import za.ac.cput.digitalpharmacysystem.util.Helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService implements IOrderService {
    private final OrderRepository repository;
    private final DeliveryRepository deliveryRepository;
    private final IProductService productService;
    private final IPrescriptionService prescriptionService;
    private final IRefillTrackerService refillTrackerService;
    private final IAuditLogService auditLogService;

    @Autowired
    public OrderService(OrderRepository repository,
                        DeliveryRepository deliveryRepository,
                        IProductService productService,
                        IPrescriptionService prescriptionService,
                        IRefillTrackerService refillTrackerService,
                        IAuditLogService auditLogService) {
        this.repository = repository;
        this.deliveryRepository = deliveryRepository;
        this.productService = productService;
        this.prescriptionService = prescriptionService;
        this.refillTrackerService = refillTrackerService;
        this.auditLogService = auditLogService;
    }
    @Override
    public List<Order> getOrdersForCurrentUser() {
        User currentUser = ActorContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated.");
        }

        // Check user role
        String role = currentUser.getRole() != null ? currentUser.getRole().name() : "";

        // Admin and Pharmacist can see all orders
        if ("ADMIN".equalsIgnoreCase(role) || "PHARMACIST".equalsIgnoreCase(role)) {
            return repository.findAll();
        }

        // Customer/Patient can only see their own orders
        return repository.findByCustomerId(currentUser.getId().toString());
    }

    @Override
    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        if (request == null
                || Helper.isNullOrEmpty(request.getCustomerId())
                || request.getOrderType() == null
                || request.getItems() == null
                || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("customerId, orderType and at least one item are required");
        }
        if (request.getOrderType() == OrderType.DELIVERY && Helper.isNullOrEmpty(request.getAddress())) {
            throw new IllegalArgumentException("A delivery address is required for delivery orders");
        }

        // Merge repeated products into one line so stock is deducted once per product.
        Map<String, Integer> quantities = new LinkedHashMap<>();
        Map<String, String> requestedPrescriptions = new HashMap<>();
        for (PlaceOrderRequest.Item item : request.getItems()) {
            if (item == null || Helper.isNullOrEmpty(item.getProductId())) {
                throw new IllegalArgumentException("Every item needs a productId");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for product " + item.getProductId());
            }
            quantities.merge(item.getProductId(), item.getQuantity(), Math::addExact);
            if (!Helper.isNullOrEmpty(item.getPrescriptionId())) {
                requestedPrescriptions.putIfAbsent(item.getProductId(), item.getPrescriptionId());
            }
        }

        // Prices come from the database, never from the client.
        Map<String, Product> products = new LinkedHashMap<>();
        Map<String, BigDecimal> prices = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> line : quantities.entrySet()) {
            Product product = productService.read(line.getKey());
            if (product == null) {
                throw new NoSuchElementException("Product not found: " + line.getKey());
            }
            if (!product.isActive()) {
                throw new IllegalStateException(product.getProductName() + " is not available");
            }
            BigDecimal price = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
            products.put(line.getKey(), product);
            prices.put(line.getKey(), price);
            total = total.add(price.multiply(BigDecimal.valueOf(line.getValue())));
        }

        // Every prescription-only product needs an approved prescription for that medicine,
        // belonging to this customer, that still has a refill available.
        Map<String, String> prescriptionByProduct = new LinkedHashMap<>();
        for (Product product : products.values()) {
            if (product.isRxRequired()) {
                prescriptionByProduct.put(product.getProductId(),
                        resolvePrescription(request.getCustomerId(), product,
                                requestedPrescriptions.get(product.getProductId())));
            }
        }

        // All-or-nothing: if anything below fails (stock short, refill gone), the whole
        // transaction rolls back, including deductions already made.
        productService.deductStock(quantities);
        prescriptionByProduct.values().forEach(refillTrackerService::processRefill);

        Order order = new Order.Builder()
                .setOrderNumber("ORD-" + UUID.randomUUID())
                .setCustomerId(request.getCustomerId())
                .setTotalAmt(total.doubleValue())
                .setOrderType(request.getOrderType())
                .setOrderStatus(OrderStatus.PENDING)
                .setPaymentStatus(PaymentStatus.PENDING)
                .setCreatedAt(LocalDateTime.now())
                .build();

        for (Map.Entry<String, Integer> line : quantities.entrySet()) {
            OrderItem item = OrderItemFactory.createOrderItem(
                    UUID.randomUUID().toString(), order, products.get(line.getKey()),
                    line.getValue(), prices.get(line.getKey()), prescriptionByProduct.get(line.getKey()));
            if (item == null) {
                throw new IllegalArgumentException("Could not create item for product " + line.getKey());
            }
            order.addItem(item);
        }

        Order saved = this.repository.save(order);

        if (saved.getOrderType() == OrderType.DELIVERY) {
            this.deliveryRepository.save(new Delivery.Builder()
                    .setId(UUID.randomUUID().toString())
                    .setTrackingNo("TRK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                    .setAddress(request.getAddress().trim())
                    .setStatus(DeliveryStatus.PENDING)
                    .setOrder(saved)
                    .build());
        }

        auditLogService.record("ORDER_PLACED", "Order", saved.getOrderNumber());
        return saved;
    }

    @Override
    public Order read(String orderNumber) {
        return this.repository.findById(orderNumber).orElse(null);
    }

    @Override
    public List<Order> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Order> getByCustomer(String customerId) {
        return this.repository.findByCustomerId(customerId).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt,
                        Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
                .toList();
    }

    @Override
    @Transactional
    public Order pay(String orderNumber) {
        Order order = find(orderNumber);
        if (order.getOrderStatus() != OrderStatus.PENDING || order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Only unpaid PENDING orders can be paid (this one is " + order.getOrderStatus() + ")");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSING);
        Order saved = this.repository.save(order);
        auditLogService.record("ORDER_PAID", "Order", orderNumber);
        return saved;
    }

    @Override
    @Transactional
    public Order pack(String orderNumber) {
        Order order = find(orderNumber);
        if (order.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Only PROCESSING orders can be packed (this one is " + order.getOrderStatus() + ")");
        }

        if (order.getOrderType() == OrderType.DELIVERY) {
            order.setOrderStatus(OrderStatus.PACKED);
            this.deliveryRepository.findByOrder_OrderNumber(orderNumber).ifPresent(delivery -> {
                delivery.setStatus(DeliveryStatus.PACKED);
                this.deliveryRepository.save(delivery);
            });
        } else {
            order.setOrderStatus(OrderStatus.READY_FOR_COLLECTION);
        }

        Order saved = this.repository.save(order);
        auditLogService.record("ORDER_PACKED", "Order", orderNumber);
        return saved;
    }

    @Override
    @Transactional
    public Order collect(String orderNumber) {
        Order order = find(orderNumber);
        if (order.getOrderStatus() != OrderStatus.READY_FOR_COLLECTION) {
            throw new IllegalStateException(
                    "Only orders READY_FOR_COLLECTION can be collected (this one is " + order.getOrderStatus() + ")");
        }
        order.setOrderStatus(OrderStatus.COLLECTED);
        Order saved = this.repository.save(order);
        auditLogService.record("ORDER_COLLECTED", "Order", orderNumber);
        return saved;
    }

    @Override
    @Transactional
    public Order cancelOrder(String orderNumber) {
        Order order = find(orderNumber);
        if (!holdsStock(order)) {
            throw new IllegalStateException(
                    "This order can no longer be cancelled (it is " + order.getOrderStatus() + ")");
        }

        // Give everything back first: the stock updates clear the persistence context, so the
        // status changes below are made afterwards and written by the final save().
        release(order);
        cancelDelivery(orderNumber);

        order.setOrderStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        Order saved = this.repository.save(order);
        auditLogService.record("ORDER_CANCELLED", "Order", orderNumber);
        return saved;
    }

    @Override
    @Transactional
    public boolean delete(String orderNumber) {
        Order order = this.repository.findById(orderNumber).orElse(null);
        if (order == null) {
            return false;
        }
        if (holdsStock(order)) {
            release(order);
        }
        // A delivery points at its order, so it has to go first.
        this.deliveryRepository.findByOrder_OrderNumber(orderNumber).ifPresent(this.deliveryRepository::delete);
        this.repository.deleteById(orderNumber);
        return true;
    }

    private Order find(String orderNumber) {
        return this.repository.findById(orderNumber)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderNumber));
    }

    /** Stock (and refills) stay reserved until the order is shipped or collected. */
    private boolean holdsStock(Order order) {
        OrderStatus status = order.getOrderStatus();
        return status == OrderStatus.PENDING
                || status == OrderStatus.PROCESSING
                || status == OrderStatus.PACKED
                || status == OrderStatus.READY_FOR_COLLECTION;
    }

    /** Puts the order's stock back and gives back the refills it used. */
    private void release(Order order) {
        // Collected up front because restoring stock clears the persistence context.
        List<OrderItem> items = order.getItems().stream()
                .sorted(Comparator.comparing((OrderItem i) -> i.getProduct().getProductId()))
                .toList();
        List<String> prescriptionIds = items.stream()
                .map(OrderItem::getPrescriptionId)
                .filter(Objects::nonNull)
                .toList();

        // Sorted by product id, matching ProductService.deductStock(Map), so concurrent
        // orders always lock rows in the same order.
        items.forEach(i -> productService.restoreStock(i.getProduct().getProductId(), i.getOrderQty()));
        prescriptionIds.forEach(refillTrackerService::restoreRefill);
    }

    private void cancelDelivery(String orderNumber) {
        this.deliveryRepository.findByOrder_OrderNumber(orderNumber).ifPresent(delivery -> {
            if (delivery.getStatus() != DeliveryStatus.DELIVERED) {
                delivery.setStatus(DeliveryStatus.CANCELLED);
                this.deliveryRepository.save(delivery);
            }
        });
    }

    /**
     * Picks (or checks) the prescription that covers a prescription-only product.
     * It must be APPROVED, belong to the customer, be for this product, and still have a
     * refill available.
     */
    private String resolvePrescription(String customerId, Product product, String requestedId) {
        if (!Helper.isNullOrEmpty(requestedId)) {
            Prescription prescription = prescriptionService.read(requestedId);
            if (prescription == null) {
                throw new NoSuchElementException("Prescription not found: " + requestedId);
            }
            if (prescription.getVerificationStatus() != VerificationStatus.APPROVED) {
                throw new IllegalStateException("Prescription " + requestedId + " is "
                        + prescription.getVerificationStatus() + ", it must be APPROVED");
            }
            if (!customerId.equals(prescription.getPatientId())) {
                throw new IllegalStateException(
                        "Prescription " + requestedId + " does not belong to customer " + customerId);
            }
            if (!product.getProductId().equals(prescription.getProductId())) {
                throw new IllegalStateException(
                        "Prescription " + requestedId + " is not for " + product.getProductName());
            }
            if (!refillTrackerService.isRefillEligible(requestedId)) {
                throw new IllegalStateException(
                        "Prescription " + requestedId + " has no refill available right now");
            }
            return requestedId;
        }

        return prescriptionService.getByPatientId(customerId).stream()
                .filter(p -> product.getProductId().equals(p.getProductId()))
                .filter(p -> refillTrackerService.isRefillEligible(p.getPrescriptionId()))
                .map(Prescription::getPrescriptionId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No approved prescription with a refill available for " + product.getProductName()));
    }
}
