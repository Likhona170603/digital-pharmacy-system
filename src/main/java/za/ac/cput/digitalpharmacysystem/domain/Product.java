package za.ac.cput.digitalpharmacysystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

/*
 * [Asenathi Nini]
 * [230452817]
 */
@Entity
public class Product {
    @Id
    private String productId;
    private String productName;
    private String description;
    private double price;
    @Column(unique = true, nullable = false)
    private String sku;
    private int stockQuantity;


    @Column(name = "is_rx_required")
    private boolean rxRequired;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    protected Product() {}

    private Product(Builder builder) {
        this.productId = builder.productId;
        this.productName = builder.productName;
        this.description = builder.description;
        this.price = builder.price;
        this.sku = builder.sku;
        this.stockQuantity = builder.stockQuantity;
        this.rxRequired = builder.rxRequired;
        this.category = builder.category;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getSku() {
        return sku;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isRxRequired() {
        return rxRequired;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", sku='" + sku + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", isRxRequired=" + rxRequired +
                ", category=" + category +
                '}';
    }

    public static class Builder {
        private String productId;
        private String productName;
        private String description;
        private double price;
        private String sku;
        private int stockQuantity;
        private boolean rxRequired;
        private Category category;

        public Builder setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder setProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setSku(String sku) {
            this.sku = sku;
            return this;
        }

        public Builder setStockQuantity(int stockQuantity) {
            this.stockQuantity = stockQuantity;
            return this;
        }

        public Builder setIsRxRequired(boolean isRxRequired) {
            this.rxRequired = isRxRequired;
            return this;
        }

        public Builder setCategory(Category category) {
            this.category = category;
            return this;
        }

        public Builder copy(Product product) {
            this.productId = product.productId;
            this.productName = product.productName;
            this.description = product.description;
            this.price = product.price;
            this.sku = product.sku;
            this.stockQuantity = product.stockQuantity;
            this.rxRequired = product.rxRequired;
            this.category = product.category;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
