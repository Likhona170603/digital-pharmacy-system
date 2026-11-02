package za.ac.cput.digitalpharmacysystem.factory;


import za.ac.cput.digitalpharmacysystem.domain.Order;
import za.ac.cput.digitalpharmacysystem.domain.OrderItem;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.util.Helper;

import java.math.BigDecimal;


public class OrderItemFactory {
    public static OrderItem createOrderItem(String orderItemId, Order order, Product product,
                                            int orderQty, BigDecimal priceAtPurchase) {
        return createOrderItem(orderItemId, order, product, orderQty, priceAtPurchase, null);
    }

    public static OrderItem createOrderItem(String orderItemId, Order order, Product product,
                                            int orderQty, BigDecimal priceAtPurchase, String prescriptionId) {
        if (Helper.isNullOrEmpty(orderItemId)
                || order == null
                || product == null
                || orderQty <= 0
                || priceAtPurchase == null
                || priceAtPurchase.signum() < 0) {
            return null;
        }
        return new OrderItem.Builder()
                .setOrderItemId(orderItemId)
                .setOrder(order)
                .setProduct(product)
                .setOrderQty(orderQty)
                .setPriceAtPurchase(priceAtPurchase)
                .setPrescriptionId(prescriptionId)
                .build();
    }
}