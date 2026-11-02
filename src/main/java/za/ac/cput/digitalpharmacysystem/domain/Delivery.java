package za.ac.cput.digitalpharmacysystem.domain;




import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;



/*
 * Sesona Ntshatsha
 * 240773365

 */
@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    private String id;

    private String trackingNo;
    private String address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @ManyToOne
    @JoinColumn(name = "order_number")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;

    private LocalDateTime dispatchedAt;
    private LocalDateTime deliveredAt;
    private String recipientName;

    protected Delivery() {}

    private Delivery(Builder builder) {
        this.id = builder.id;
        this.trackingNo = builder.trackingNo;
        this.address = builder.address;
        this.status = builder.status;
        this.order = builder.order;
        this.courier = builder.courier;
        this.dispatchedAt = builder.dispatchedAt;
        this.deliveredAt = builder.deliveredAt;
        this.recipientName = builder.recipientName;
    }

    public String getId() {
        return id;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public String getAddress() {
        return address;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Order getOrder() {
        return order;
    }

    public User getCourier() {
        return courier;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setDispatchedAt(LocalDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setCourier(User courier) {
        this.courier = courier;
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "id='" + id + '\'' +
                ", trackingNo='" + trackingNo + '\'' +
                ", address='" + address + '\'' +
                ", status=" + status +
                ", orderNumber=" +
                (order != null ? order.getOrderNumber() : null) +
                ", courierId=" +
                (courier != null ? courier.getId() : null) +
                '}';
    }

    public static class Builder {
        private String id;
        private String trackingNo;
        private String address;
        private DeliveryStatus status;
        private Order order;
        private User courier;
        private LocalDateTime dispatchedAt;
        private LocalDateTime deliveredAt;
        private String recipientName;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTrackingNo(String trackingNo) {
            this.trackingNo = trackingNo;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setStatus(DeliveryStatus status) {
            this.status = status;
            return this;
        }

        public Builder setOrder(Order order) {
            this.order = order;
            return this;
        }

        public Builder setCourier(User courier) {
            this.courier = courier;
            return this;
        }

        public Builder setDispatchedAt(LocalDateTime dispatchedAt) {
            this.dispatchedAt = dispatchedAt;
            return this;
        }

        public Builder setDeliveredAt(LocalDateTime deliveredAt) {
            this.deliveredAt = deliveredAt;
            return this;
        }

        public Builder setRecipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }

        public Delivery build() {
            return new Delivery(this);
        }
    }
}