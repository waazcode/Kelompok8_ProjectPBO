package Kelompok8;

public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }

    public double subtotal() { return product.getPrice() * quantity; }

    @Override
    public String toString() {
        return String.format("%s x%d => Rp %.0f", product.getName(), quantity, subtotal());
    }
}
