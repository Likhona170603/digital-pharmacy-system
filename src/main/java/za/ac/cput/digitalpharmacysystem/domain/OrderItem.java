package za.ac.cput.digitalpharmacysystem.domain;
/*
 * Sibulele Pama
 * 221447598
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")

public class OrderItem {

    @Id
    private String orderItemId;


    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_number")
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    private int orderQty;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    // Set for prescription-only products: the prescription that was used for this line.
    private String prescriptionId;

    protected OrderItem() {
    }

    private OrderItem(Builder builder) {
        this.orderItemId = builder.orderItemId;
        this.order = builder.order;
        this.product = builder.product;
        this.orderQty = builder.orderQty;
        this.priceAtPurchase = builder.priceAtPurchase;
        this.prescriptionId = builder.prescriptionId;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public int getOrderQty() {
        return orderQty;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public static class Builder {
        private String orderItemId;
        private Order order;
        private Product product;
        private int orderQty;
        private BigDecimal priceAtPurchase;
        private String prescriptionId;

        public Builder setOrderItemId(String orderItemId) {
            this.orderItemId = orderItemId;
            return this;
        }

        public Builder setOrder(Order order) {
            this.order = order;
            return this;
        }

        public Builder setProduct(Product product) {
            this.product = product;
            return this;
        }

        public Builder setOrderQty(int orderQty) {
            this.orderQty = orderQty;
            return this;
        }

        public Builder setPriceAtPurchase(BigDecimal priceAtPurchase) {
            this.priceAtPurchase = priceAtPurchase;
            return this;
        }

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}