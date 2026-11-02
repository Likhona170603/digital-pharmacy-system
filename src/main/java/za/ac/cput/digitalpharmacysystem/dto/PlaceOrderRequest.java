package za.ac.cput.digitalpharmacysystem.dto;

import za.ac.cput.digitalpharmacysystem.domain.OrderType;

import java.util.ArrayList;
import java.util.List;


public class PlaceOrderRequest {

    private String customerId;
    private OrderType orderType;
    private String address;            // required when orderType is DELIVERY
    private List<Item> items = new ArrayList<>();

    public PlaceOrderRequest() {
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        private String productId;
        private int quantity;
        private String prescriptionId;

        public Item() {
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getPrescriptionId() {
            return prescriptionId;
        }

        public void setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
        }
    }
}