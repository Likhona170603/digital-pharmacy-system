package za.ac.cput.digitalpharmacysystem.domain;
/*
 * Sibulele Pama
 * 221447598
 */

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")

public class Order {
    @Id
    private String orderNumber;
    private String customerId;
    private double totalAmt;

    private OrderType orderType;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }


    private Order(Builder builder) {
        this.orderNumber = builder.orderNumber;
        this.customerId = builder.customerId;
        this.totalAmt = builder.totalAmt;
        this.orderType = builder.orderType;
        this.orderStatus = builder.orderStatus;
        this.paymentStatus = builder.paymentStatus;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return orderNumber;
    }

    public static class Builder {
        private String orderNumber;
        private String customerId;
        private double totalAmt;
        private OrderType orderType;
        private OrderStatus orderStatus;
        private PaymentStatus paymentStatus;
        private LocalDateTime createdAt;

        public Builder setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setTotalAmt(double totalAmt) {
            this.totalAmt = totalAmt;
            return this;
        }

        public Builder setOrderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder setOrderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }

        public Builder setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

    //Getters
    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getTotalAmt() {
        return totalAmt;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }


    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }
}
