package Kelompok8;

public class Product {
    private final int id;
    private String name;
    private String description;
    private double price;
    private int stock;

    public Product(int id, String name, String description, double price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Encapsulation: private fields with getters/setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }

    public void reduceStock(int qty) { this.stock -= qty; }

    @Override
    public String toString() {
        return String.format("[%d] %s - Rp %.0f | stok: %d | %s", id, name, price, stock, description);
    }
}
