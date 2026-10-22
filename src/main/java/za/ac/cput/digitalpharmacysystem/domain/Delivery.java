package za.ac.cput.digitalpharmacysystem.domain;




import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;



/*
 * Sesona Ntshatsha
 * 240773365

 */
@Entity
public class Delivery {
    @Id
    private String id;
    private String trackingNo;
    private String address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    protected Delivery() {}

    private Delivery (Builder builder) {
        this.id = builder.id;
        this.trackingNo = builder.trackingNo;
        this.address = builder.address;
        this.status = builder.status;
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
    @Override
    public String toString() {
        return "Delivery{" +
                "id=" + id +
                ", trackingNo='" + trackingNo + '\'' +
                ", address='" + address + '\'' +
                ", status=" + status +
                '}';
    }

    public static class Builder {
        private String id;
        private String trackingNo;
        private String address;
        private DeliveryStatus status;

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
        public Builder setStatus(DeliveryStatus status){
            this.status = status;
            return this;
        }
        public Delivery build() {
            return new Delivery(this);

        }
    }


}
