package za.ac.cput.digitalpharmacysystem.dto;

/*
 * Asenathi Nini
 * 230452817
 */
public class StockRequest {
    private int quantity;

    public StockRequest() {
    }

    public StockRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockRequest that = (StockRequest) o;
        return quantity == that.quantity;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(quantity);
    }

    @Override
    public String toString() {
        return "StockRequest{quantity=" + quantity + '}';
    }
}