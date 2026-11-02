package za.ac.cput.digitalpharmacysystem.exception;

/*
 * Asenathi Nini
 * 230452817
 */
public class InsufficientStockException extends RuntimeException {

    private final String productId;
    private final int requested;

    public InsufficientStockException(String productId, int requested) {
        super("Insufficient stock for product " + productId + " (requested " + requested + ")");
        this.productId = productId;
        this.requested = requested;
    }

    public String getProductId() {
        return productId;
    }

    public int getRequested() {
        return requested;
    }
}