package za.ac.cput.digitalpharmacysystem.factory;

import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.util.Helper;

/*
 * Asenathi Nini
 * 230452817
 */
public class ProductFactory {

    public static Product createProduct(String productId, String productName, String description, double price, String sku, int stockQuantity, boolean isRxRequired, Category category) {
        if (Helper.isNullOrEmpty(productId)) {
            return null;
        }
        if (Helper.isNullOrEmpty(productName)) {
            return null;
        }
        if (Helper.isNullOrEmpty(sku)) {
            return null;
        }
        if (price < 0) {
            return null;
        }
        if (stockQuantity < 0) {
            return null;
        }
        if (category == null) {
            return null;
        }

        return new Product.Builder()
                .setProductId(productId)
                .setProductName(productName)
                .setDescription(description)
                .setPrice(price)
                .setSku(sku)
                .setStockQuantity(stockQuantity)
                .setIsRxRequired(isRxRequired)
                .setCategory(category)
                .build();
    }
}