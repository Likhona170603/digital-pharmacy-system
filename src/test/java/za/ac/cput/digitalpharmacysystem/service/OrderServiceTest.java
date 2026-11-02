package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import za.ac.cput.digitalpharmacysystem.domain.*;
import za.ac.cput.digitalpharmacysystem.dto.PlaceOrderRequest;
import za.ac.cput.digitalpharmacysystem.exception.InsufficientStockException;
import za.ac.cput.digitalpharmacysystem.factory.CategoryFactory;
import za.ac.cput.digitalpharmacysystem.factory.PrescriptionFactory;
import za.ac.cput.digitalpharmacysystem.factory.ProductFactory;
import za.ac.cput.digitalpharmacysystem.factory.UserFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests (same style as the other service tests: real Spring context and database).
 * They follow the whole purchase journey: placing orders, stock, prescriptions and refills,
 * payment, packing, collection, delivery, cancelling, and the audit trail.
 *
 * Customers get a random suffix so the tests can be re-run against the same database.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceTest {

    @Autowired
    private IOrderService service;

    @Autowired
    private IProductService productService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IPrescriptionService prescriptionService;

    @Autowired
    private IRefillTrackerService refillTrackerService;

    @Autowired
    private IDeliveryService deliveryService;

    @Autowired
    private IAuditLogService auditLogService;

    @Autowired
    private IUserService userService;

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8);
    private static final String CUSTOMER = "PAT-ORD-" + SUFFIX;
    private static final String OTHER_CUSTOMER = "PAT-OTH-" + SUFFIX;
    private static final String CUSTOMER_WITHOUT_RX = "PAT-NONE-" + SUFFIX;
    private static final String ADDRESS = "12 Long Street, Cape Town";

    private static final Category category = CategoryFactory.createCategory(
            "C-ORD-001", "Order Test Category", "Tablet", "Schedule 2");

    private static final Product otc = ProductFactory.createProduct(
            "P-ORD-001", "Order Test Painkiller", "OTC", 50.00, "SKU-ORD-001", 10, false, category);

    private static final Product rx = ProductFactory.createProduct(
            "P-ORD-002", "Order Test Antibiotic", "Rx only", 120.00, "SKU-ORD-002", 5, true, category);

    private static final Product retired = ProductFactory.createProduct(
            "P-ORD-003", "Order Test Retired Item", "Deactivated", 10.00, "SKU-ORD-003", 5, false, category);

    // approved, 2 dispenses allowed
    private static final Prescription approvedRx = PrescriptionFactory.createPrescription(
            CUSTOMER, rx.getProductId(), "prescriptions/order-test-approved.pdf", LocalDate.now());

    // stays pending until the decisions test
    private static final Prescription pendingRx = PrescriptionFactory.createPrescription(
            CUSTOMER, rx.getProductId(), "prescriptions/order-test-pending.pdf", LocalDate.now());

    // approved, but for another patient
    private static final Prescription otherPatientsRx = PrescriptionFactory.createPrescription(
            OTHER_CUSTOMER, rx.getProductId(), "prescriptions/order-test-other.pdf", LocalDate.now());

    // approved for this customer, but for a different medicine
    private static final Prescription wrongProductRx = PrescriptionFactory.createPrescription(
            CUSTOMER, otc.getProductId(), "prescriptions/order-test-wrong.pdf", LocalDate.now());

    private static String deliveryOrderNumber;
    private static String firstRxOrderNumber;
    private static String secondRxOrderNumber;

    private PlaceOrderRequest request(String customerId, OrderType type, String productId, int quantity, String prescriptionId) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setCustomerId(customerId);
        request.setOrderType(type);
        request.setAddress(type == OrderType.DELIVERY ? ADDRESS : null);

        PlaceOrderRequest.Item item = new PlaceOrderRequest.Item();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrescriptionId(prescriptionId);
        request.setItems(List.of(item));
        return request;
    }

    private int stockOf(Product product) {
        return productService.read(product.getProductId()).getStockQuantity();
    }

    private boolean audited(String action, String entityId) {
        return auditLogService.getAll().stream()
                .anyMatch(a -> action.equals(a.getAction()) && entityId.equals(a.getEntityId()));
    }

    @Test
    @Order(1)
    void setup() {
        categoryService.create(category);
        productService.create(otc);   // saving again resets stock, so the tests can be re-run
        productService.create(rx);
        productService.create(retired);
        productService.setActive(retired.getProductId(), false);

        prescriptionService.create(approvedRx);
        prescriptionService.approvePrescription(approvedRx.getPrescriptionId(), 2, "Ph. Test");
        prescriptionService.create(pendingRx);
        prescriptionService.create(otherPatientsRx);
        prescriptionService.approvePrescription(otherPatientsRx.getPrescriptionId(), 1, "Ph. Test");
        prescriptionService.create(wrongProductRx);
        prescriptionService.approvePrescription(wrongProductRx.getPrescriptionId(), 1, "Ph. Test");

        assertEquals(10, stockOf(otc));
        assertEquals(5, stockOf(rx));

        // approving created the refill tracker with the number of dispenses the pharmacist chose
        RefillTracker tracker = refillTrackerService.getByPrescriptionId(approvedRx.getPrescriptionId());
        assertNotNull(tracker);
        assertEquals(2, tracker.getTotalRefillsAllowed());
        assertEquals(0, tracker.getRefillsUsed());
        assertEquals(RefillStatus.AVAILABLE, tracker.getRefillStatus());

        Prescription approved = prescriptionService.read(approvedRx.getPrescriptionId());
        assertEquals(VerificationStatus.APPROVED, approved.getVerificationStatus());
        assertEquals("Ph. Test", approved.getVerifiedBy());
        assertNotNull(approved.getVerifiedAt());
        assertTrue(audited("PRESCRIPTION_APPROVED", approvedRx.getPrescriptionId()));
    }

    @Test
    @Order(2)
    void placeOrder_delivery_deductsStock_pricesFromProduct_createsDelivery() {
        var order = service.placeOrder(request(CUSTOMER, OrderType.DELIVERY, otc.getProductId(), 3, null));
        deliveryOrderNumber = order.getOrderNumber();

        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
        assertEquals(150.00, order.getTotalAmt(), 0.001);
        assertNotNull(order.getCreatedAt());
        assertEquals(1, order.getItems().size());
        assertEquals(3, order.getItems().get(0).getOrderQty());
        assertEquals(0, new BigDecimal("50.00").compareTo(order.getItems().get(0).getPriceAtPurchase()));
        assertNull(order.getItems().get(0).getPrescriptionId());
        assertEquals(7, stockOf(otc));

        Delivery delivery = deliveryService.findByOrderNumber(deliveryOrderNumber);
        assertNotNull(delivery);
        assertEquals(DeliveryStatus.PENDING, delivery.getStatus());
        assertEquals(ADDRESS, delivery.getAddress());
        assertTrue(delivery.getTrackingNo().startsWith("TRK"));

        assertTrue(audited("ORDER_PLACED", deliveryOrderNumber));
    }

    @Test
    @Order(3)
    void placeOrder_invalidRequests() {
        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, otc.getProductId(), 0, null)));
        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder(request("", OrderType.CLICK_AND_COLLECT, otc.getProductId(), 1, null)));
        assertThrows(NoSuchElementException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, "P-DOES-NOT-EXIST", 1, null)));

        PlaceOrderRequest noAddress = request(CUSTOMER, OrderType.DELIVERY, otc.getProductId(), 1, null);
        noAddress.setAddress(" ");
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder(noAddress));

        // deactivated products cannot be ordered
        assertThrows(IllegalStateException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, retired.getProductId(), 1, null)));

        assertEquals(7, stockOf(otc));
    }

    @Test
    @Order(4)
    void placeOrder_insufficientStock_changesNothing() {
        assertThrows(InsufficientStockException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, otc.getProductId(), 100, null)));
        assertEquals(7, stockOf(otc));
    }

    @Test
    @Order(5)
    void placeOrder_rx_rules() {
        // customer has no prescription at all
        assertThrows(IllegalStateException.class,
                () -> service.placeOrder(request(CUSTOMER_WITHOUT_RX, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, null)));
        // asked for a prescription that does not exist
        assertThrows(NoSuchElementException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, "no-such-prescription")));
        // still pending
        assertThrows(IllegalStateException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, pendingRx.getPrescriptionId())));
        // approved, but belongs to someone else
        assertThrows(IllegalStateException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, otherPatientsRx.getPrescriptionId())));
        // approved, but for a different medicine
        assertThrows(IllegalStateException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, wrongProductRx.getPrescriptionId())));

        assertEquals(5, stockOf(rx));
    }

    @Test
    @Order(6)
    void placeOrder_rx_withExplicitPrescription_usesARefill() {
        var order = service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, approvedRx.getPrescriptionId()));
        firstRxOrderNumber = order.getOrderNumber();

        assertEquals(approvedRx.getPrescriptionId(), order.getItems().get(0).getPrescriptionId());
        assertEquals(120.00, order.getTotalAmt(), 0.001);
        assertNull(deliveryService.findByOrderNumber(firstRxOrderNumber)); // collect orders have no delivery
        assertEquals(4, stockOf(rx));

        RefillTracker tracker = refillTrackerService.getByPrescriptionId(approvedRx.getPrescriptionId());
        assertEquals(1, tracker.getRefillsUsed());
        assertEquals(RefillStatus.NOT_ELIGIBLE, tracker.getRefillStatus()); // next dispense in 30 days
        assertTrue(audited("REFILL_USED", tracker.getRefillTrackerId()));
    }

    @Test
    @Order(7)
    void placeOrder_rx_refillNotAvailableYet() {
        assertThrows(IllegalStateException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, approvedRx.getPrescriptionId())));
        assertThrows(IllegalStateException.class,
                () -> service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, null)));
        assertEquals(4, stockOf(rx));
    }

    @Test
    @Order(8)
    void cancelOrder_restoresStock_andCancelsDelivery() {
        var cancelled = service.cancelOrder(deliveryOrderNumber);

        assertEquals(OrderStatus.CANCELLED, cancelled.getOrderStatus());
        assertEquals(10, stockOf(otc));
        assertEquals(DeliveryStatus.CANCELLED, deliveryService.findByOrderNumber(deliveryOrderNumber).getStatus());
        assertTrue(audited("ORDER_CANCELLED", deliveryOrderNumber));
    }

    @Test
    @Order(9)
    void cancelOrder_twiceOrUnknown_isRejected() {
        assertThrows(IllegalStateException.class, () -> service.cancelOrder(deliveryOrderNumber));
        assertThrows(NoSuchElementException.class, () -> service.cancelOrder("ORD-DOES-NOT-EXIST"));
        assertEquals(10, stockOf(otc));
    }

    @Test
    @Order(10)
    void cancelOrder_givesTheRefillBack_soItCanBeUsedAgain() {
        service.cancelOrder(firstRxOrderNumber);

        assertEquals(5, stockOf(rx));
        RefillTracker tracker = refillTrackerService.getByPrescriptionId(approvedRx.getPrescriptionId());
        assertEquals(0, tracker.getRefillsUsed());
        assertEquals(RefillStatus.AVAILABLE, tracker.getRefillStatus());

        // no prescriptionId given: the server picks the customer's eligible prescription for this medicine
        var order = service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, rx.getProductId(), 1, null));
        secondRxOrderNumber = order.getOrderNumber();

        assertEquals(approvedRx.getPrescriptionId(), order.getItems().get(0).getPrescriptionId());
        assertEquals(4, stockOf(rx));
        assertEquals(1, refillTrackerService.getByPrescriptionId(approvedRx.getPrescriptionId()).getRefillsUsed());
    }

    @Test
    @Order(11)
    void clickAndCollect_payPackCollect() {
        assertThrows(IllegalStateException.class, () -> service.pack(secondRxOrderNumber));    // not paid yet

        var paid = service.pay(secondRxOrderNumber);
        assertEquals(PaymentStatus.PAID, paid.getPaymentStatus());
        assertEquals(OrderStatus.PROCESSING, paid.getOrderStatus());
        assertThrows(IllegalStateException.class, () -> service.pay(secondRxOrderNumber));     // only once

        assertThrows(IllegalStateException.class, () -> service.collect(secondRxOrderNumber)); // not packed yet
        assertEquals(OrderStatus.READY_FOR_COLLECTION, service.pack(secondRxOrderNumber).getOrderStatus());
        assertEquals(OrderStatus.COLLECTED, service.collect(secondRxOrderNumber).getOrderStatus());

        assertThrows(IllegalStateException.class, () -> service.cancelOrder(secondRxOrderNumber));
        assertEquals(4, stockOf(rx)); // collected stock stays sold
        assertTrue(audited("ORDER_PAID", secondRxOrderNumber));
        assertTrue(audited("ORDER_PACKED", secondRxOrderNumber));
        assertTrue(audited("ORDER_COLLECTED", secondRxOrderNumber));
    }

    @Test
    @Order(12)
    void delivery_payPackAssignDispatchSignOff() {
        var order = service.placeOrder(request(CUSTOMER, OrderType.DELIVERY, otc.getProductId(), 1, null));
        String orderNumber = order.getOrderNumber();
        assertEquals(9, stockOf(otc));

        service.pay(orderNumber);
        assertEquals(OrderStatus.PACKED, service.pack(orderNumber).getOrderStatus());

        Delivery delivery = deliveryService.findByOrderNumber(orderNumber);
        assertEquals(DeliveryStatus.PACKED, delivery.getStatus());
        String deliveryId = delivery.getId();

        // a courier is needed before dispatching
        assertThrows(IllegalStateException.class,
                () -> deliveryService.updateStatus(deliveryId, DeliveryStatus.DISPATCHED, null));

        User courier = userService.create(UserFactory.buildUser(
                "Test Courier", "courier-" + SUFFIX + "@example.com", "hash", "0800000000", User.UserRole.COURIER));
        User customerUser = userService.create(UserFactory.buildUser(
                "Test Customer", "customer-" + SUFFIX + "@example.com", "hash", "0800000001", User.UserRole.CUSTOMER));
        assertThrows(NoSuchElementException.class, () -> deliveryService.assignCourier(deliveryId, -1L));
        assertThrows(IllegalArgumentException.class, () -> deliveryService.assignCourier(deliveryId, customerUser.getId()));
        assertEquals(courier.getId(), deliveryService.assignCourier(deliveryId, courier.getId()).getCourier().getId());
        assertTrue(deliveryService.getCourierManifest(courier.getId()).stream().anyMatch(d -> d.getId().equals(deliveryId)));

        // cannot skip dispatching
        assertThrows(IllegalStateException.class,
                () -> deliveryService.updateStatus(deliveryId, DeliveryStatus.DELIVERED, "T. Test"));

        Delivery dispatched = deliveryService.updateStatus(deliveryId, DeliveryStatus.DISPATCHED, null);
        assertEquals(DeliveryStatus.DISPATCHED, dispatched.getStatus());
        assertNotNull(dispatched.getDispatchedAt());
        assertEquals(OrderStatus.SHIPPED, service.read(orderNumber).getOrderStatus());

        // sign-off needs the recipient's name
        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.updateStatus(deliveryId, DeliveryStatus.DELIVERED, " "));

        Delivery delivered = deliveryService.updateStatus(deliveryId, DeliveryStatus.DELIVERED, "T. Test");
        assertEquals(DeliveryStatus.DELIVERED, delivered.getStatus());
        assertEquals("T. Test", delivered.getRecipientName());
        assertNotNull(delivered.getDeliveredAt());
        assertEquals(OrderStatus.DELIVERED, service.read(orderNumber).getOrderStatus());

        assertThrows(IllegalStateException.class, () -> service.cancelOrder(orderNumber));
        assertEquals(9, stockOf(otc));

        assertTrue(audited("COURIER_ASSIGNED", deliveryId));
        assertTrue(audited("DELIVERY_DISPATCHED", deliveryId));
        assertTrue(audited("DELIVERY_SIGNED_OFF", deliveryId));
    }

    @Test
    @Order(13)
    void delete_activeOrders_giveStockBack() {
        var collect = service.placeOrder(request(CUSTOMER, OrderType.CLICK_AND_COLLECT, otc.getProductId(), 2, null));
        assertEquals(7, stockOf(otc));
        assertTrue(service.delete(collect.getOrderNumber()));
        assertNull(service.read(collect.getOrderNumber()));
        assertEquals(9, stockOf(otc));

        var delivery = service.placeOrder(request(CUSTOMER, OrderType.DELIVERY, otc.getProductId(), 1, null));
        assertEquals(8, stockOf(otc));
        assertTrue(service.delete(delivery.getOrderNumber()));
        assertNull(deliveryService.findByOrderNumber(delivery.getOrderNumber()));
        assertEquals(9, stockOf(otc));

        assertFalse(service.delete(delivery.getOrderNumber()));
    }

    @Test
    @Order(14)
    void history_isNewestFirst_andCatalogueHidesInactiveProducts() {
        List<za.ac.cput.digitalpharmacysystem.domain.Order> history = service.getByCustomer(CUSTOMER);
        assertTrue(history.size() >= 4);
        for (int i = 1; i < history.size(); i++) {
            LocalDateTime newer = history.get(i - 1).getCreatedAt();
            LocalDateTime older = history.get(i).getCreatedAt();
            assertFalse(newer.isBefore(older));
        }
        assertTrue(service.getByCustomer(CUSTOMER_WITHOUT_RX).isEmpty());

        List<String> catalogue = productService
                .search("Order Test", null, null, false, true, PageRequest.of(0, 50))
                .map(Product::getProductId).getContent();
        assertTrue(catalogue.contains(otc.getProductId()));
        assertFalse(catalogue.contains(retired.getProductId()));

        List<String> everything = productService
                .search("Order Test", null, null, false, false, PageRequest.of(0, 50))
                .map(Product::getProductId).getContent();
        assertTrue(everything.contains(retired.getProductId()));
    }

    @Test
    @Order(15)
    void prescriptionDecisions_needAReason_andAreFinal() {
        String id = pendingRx.getPrescriptionId();

        assertThrows(IllegalArgumentException.class, () -> prescriptionService.requestClarification(id, " ", "Ph. Test"));

        Prescription clarify = prescriptionService.requestClarification(id, "Scan is blurry", "Ph. Test");
        assertEquals(VerificationStatus.NEEDS_CLARIFICATION, clarify.getVerificationStatus());
        assertEquals("Scan is blurry", clarify.getRejectionReason());

        Prescription rejected = prescriptionService.rejectPrescription(id, "Expired", "Ph. Test");
        assertEquals(VerificationStatus.REJECTED, rejected.getVerificationStatus());
        assertEquals("Ph. Test", rejected.getVerifiedBy());
        assertTrue(audited("PRESCRIPTION_REJECTED", id));

        assertThrows(IllegalStateException.class, () -> prescriptionService.approvePrescription(id, 1, "Ph. Test"));
        assertThrows(NoSuchElementException.class, () -> prescriptionService.approvePrescription("no-such-id", 1, "Ph. Test"));
    }
}
