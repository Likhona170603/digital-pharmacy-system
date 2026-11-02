package za.ac.cput.digitalpharmacysystem.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.digitalpharmacysystem.domain.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemFactoryTest {

    private final Category category = CategoryFactory.createCategory("C-OIF-1", "Cat", "Tablet", "Schedule 1");
    private final Product product = ProductFactory.createProduct("P-OIF-1", "Prod", "desc", 10.0, "SKU-OIF-1", 5, false, category);
    private final Order order = new Order.Builder()
            .setOrderNumber("ORD-OIF-1").setCustomerId("CUST-1").setTotalAmt(10.0)
            .setOrderType(OrderType.DELIVERY).setOrderStatus(OrderStatus.PENDING)
            .setPaymentStatus(PaymentStatus.PENDING).build();

    @Test
    void createOrderItem_valid() {
        OrderItem item = OrderItemFactory.createOrderItem("OI-1", order, product, 2, new BigDecimal("10.00"));

        assertNotNull(item);
        assertEquals("OI-1", item.getOrderItemId());
        assertSame(order, item.getOrder());
        assertSame(product, item.getProduct());
        assertEquals(2, item.getOrderQty());
        assertEquals(new BigDecimal("10.00"), item.getPriceAtPurchase());
    }

    @Test
    void createOrderItem_invalidInputReturnsNull() {
        BigDecimal price = new BigDecimal("10.00");
        assertNull(OrderItemFactory.createOrderItem("", order, product, 2, price));
        assertNull(OrderItemFactory.createOrderItem("OI-1", null, product, 2, price));
        assertNull(OrderItemFactory.createOrderItem("OI-1", order, null, 2, price));
        assertNull(OrderItemFactory.createOrderItem("OI-1", order, product, 0, price));
        assertNull(OrderItemFactory.createOrderItem("OI-1", order, product, 2, null));
        assertNull(OrderItemFactory.createOrderItem("OI-1", order, product, 2, new BigDecimal("-1")));
    }

    @Test
    void createOrderItem_keepsPrescriptionId() {
        OrderItem item = OrderItemFactory.createOrderItem("OI-2", order, product, 1, new BigDecimal("10.00"), "RX-1");

        assertNotNull(item);
        assertEquals("RX-1", item.getPrescriptionId());
        assertNull(OrderItemFactory.createOrderItem("OI-3", order, product, 1, new BigDecimal("10.00")).getPrescriptionId());
    }
}
