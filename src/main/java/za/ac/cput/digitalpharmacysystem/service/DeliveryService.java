package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.digitalpharmacysystem.domain.*;
import za.ac.cput.digitalpharmacysystem.repository.DeliveryRepository;
import za.ac.cput.digitalpharmacysystem.repository.IOrderRepository;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;
import za.ac.cput.digitalpharmacysystem.util.ActorContext;
import za.ac.cput.digitalpharmacysystem.util.Helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@Service
public class DeliveryService implements IDeliveryService{
    private final DeliveryRepository repository;
    private final IOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IAuditLogService auditLogService;


    @Autowired
    public DeliveryService(
            DeliveryRepository repository,
            IOrderRepository orderRepository,
            UserRepository userRepository,
            IAuditLogService auditLogService) {
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

        @Override
        public Delivery create(Delivery delivery) {
            return this.repository.save(delivery);
        }

        @Override
        public Delivery read(String id) {
            return this.repository.findById(id).orElse(null);
        }

    @Override
    public List<Delivery> getDeliveriesForCurrentUser() {
        User currentUser = ActorContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated.");
        }

        String role = currentUser.getRole() != null ? currentUser.getRole().name() : "";

        // Admin and Pharmacist can see all deliveries
        if ("ADMIN".equalsIgnoreCase(role) || "PHARMACIST".equalsIgnoreCase(role)) {
            return repository.findAll();
        }

        // Couriers can only see deliveries assigned to them
        // (Make sure your deliveryRepository has a method matching this query,
        // e.g. findByCourierId or findByCourier_UserId)
        return repository.findByCourier_Id(currentUser.getId());
    }
        @Override
        public Delivery update(Delivery delivery) {
            if (this.repository.existsById(delivery.getId())) {
                return this.repository.save(delivery);
            }
            return null;
        }

        @Override
        public boolean delete(String id) {
            if (this.repository.existsById(id)) {
                this.repository.deleteById(id);
                return true;
            }
            return false;
        }

        @Override
        public List<Delivery> getAll() {
            return this.repository.findAll();
        }

    public Delivery findByOrderNumber(String orderNumber) {
        return repository.findByOrder_OrderNumber(orderNumber)
                .orElse(null);
    }
    public List<Delivery> getCourierManifest(Long courierId) {
        return repository.findByCourier_Id(courierId);
    }

    // Which status each delivery may move to. PENDING -> PACKED happens when the pharmacist
    // packs the order (OrderService.pack), and CANCELLED when the order is cancelled.
    private static final Map<DeliveryStatus, Set<DeliveryStatus>> ALLOWED = Map.of(
            DeliveryStatus.PACKED, Set.of(DeliveryStatus.DISPATCHED),
            DeliveryStatus.DISPATCHED, Set.of(DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELIVERED),
            DeliveryStatus.IN_TRANSIT, Set.of(DeliveryStatus.DELIVERED));

    @Override
    @Transactional
    public Delivery updateStatus(String id, DeliveryStatus newStatus, String recipientName) {
        Delivery delivery = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Delivery not found: " + id));

        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }

        Set<DeliveryStatus> allowed = ALLOWED.getOrDefault(delivery.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    "Cannot move a delivery from " + delivery.getStatus() + " to " + newStatus);
        }
        if (newStatus == DeliveryStatus.DISPATCHED && delivery.getCourier() == null) {
            throw new IllegalStateException("Assign a courier before dispatching");
        }
        if (newStatus == DeliveryStatus.DELIVERED && Helper.isNullOrEmpty(recipientName)) {
            throw new IllegalArgumentException("Recipient name is required for delivery sign-off");
        }

        delivery.setStatus(newStatus);
        if (newStatus == DeliveryStatus.DISPATCHED) {
            delivery.setDispatchedAt(LocalDateTime.now());
        }
        if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.setRecipientName(recipientName.trim());
        }
        Delivery saved = repository.save(delivery);

        syncOrder(saved, newStatus);

        String action = switch (newStatus) {
            case DISPATCHED -> "DELIVERY_DISPATCHED";
            case IN_TRANSIT -> "DELIVERY_IN_TRANSIT";
            default -> "DELIVERY_SIGNED_OFF";
        };
        auditLogService.record(action, "Delivery", saved.getId());

        return saved;
    }

    @Override
    @Transactional
    public Delivery assignCourier(String id, Long courierId) {
        Delivery delivery = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Delivery not found: " + id));
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + courierId));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User " + courierId + " is not a courier");
        }
        if (delivery.getStatus() == DeliveryStatus.DELIVERED
                || delivery.getStatus() == DeliveryStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot assign a courier to a " + delivery.getStatus() + " delivery");
        }

        delivery.setCourier(courier);
        Delivery saved = repository.save(delivery);

        auditLogService.record("COURIER_ASSIGNED", "Delivery", saved.getId());

        return saved;
    }

    /** Keeps the order's status in step with its delivery. */
    private void syncOrder(Delivery delivery, DeliveryStatus status) {
        Order order = delivery.getOrder();
        if (order == null) {
            return;
        }
        OrderStatus current = order.getOrderStatus();

        if (status == DeliveryStatus.DISPATCHED && current == OrderStatus.PACKED) {
            order.setOrderStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        } else if (status == DeliveryStatus.DELIVERED
                && (current == OrderStatus.PACKED || current == OrderStatus.SHIPPED)) {
            order.setOrderStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }
    }
}
