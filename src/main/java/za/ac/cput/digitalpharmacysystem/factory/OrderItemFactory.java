package za.ac.cput.digitalpharmacysystem.factory;


import za.ac.cput.digitalpharmacysystem.domain.OrderItem;


public class OrderItemFactory {
    public static OrderItem createOrderItem(String orderId, String orderQty, String priceAtPurchase, String productId) {
        return new OrderItem.Builder()
                .setOrderId(orderId)
                .setOrderQty(orderQty)
                .setPriceAtPurchase(priceAtPurchase)
//                .setProductId(productId)
                .build();
    }
}
