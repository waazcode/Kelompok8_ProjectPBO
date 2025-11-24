package Kelompok8;

import java.io.*;
import java.util.*;

/**
 * Store: menyimpan collection users, products, orders.
 * Users persist ke users.txt (format per baris: username;password;role).
 */
public class Store {
    private final Map<String, User> users = new LinkedHashMap<>(); // username -> User
    private final Map<Integer, Product> products = new LinkedHashMap<>(); // id -> Product
    private final List<Order> orders = new ArrayList<>();

    private final String USER_FILE = "users.txt";
    private final String PRODUCT_FILE = "products.txt";

    public Store() {
        loadUsers();
        loadProducts();
    }

    private void seedProducts() {
        addProductInternal(new Product(1, "Canon EOS R10", "Mirrorless 24MP", 15000000, 5), false);
        addProductInternal(new Product(2, "Sony A7 IV", "Full-frame 33MP", 45000000, 3), false);
        addProductInternal(new Product(3, "Tripod Pro", "Tripod ringan", 450000, 10), false);
        addProductInternal(new Product(4, "Fujifilm X-S20", "APS-C hybrid camera", 21000000, 4), false);
        addProductInternal(new Product(5, "Nikon Z6 II", "Full-frame 24MP", 32000000, 2), false);
        addProductInternal(new Product(6, "GoPro Hero 12", "Action cam 5.3K", 8500000, 7), false);
        addProductInternal(new Product(7, "Sigma 35mm f/1.4", "Lensa prime art", 14500000, 6), false);
        addProductInternal(new Product(8, "Godox SL60W", "Continuous light kit", 2200000, 9), false);
        addProductInternal(new Product(9, "Peak Design Everyday Backpack", "Tas kamera premium 20L", 4800000, 8), false);
        saveProductsToFile();
    }

    private void loadProducts() {
        File file = new File(PRODUCT_FILE);
        if (!file.exists()) {
            seedProducts();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 5) continue;
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                String desc = parts[2];
                double price = Double.parseDouble(parts[3]);
                int stock = Integer.parseInt(parts[4]);
                addProductInternal(new Product(id, name, desc, price, stock), false);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Gagal load products: " + e.getMessage());
        }
        if (products.isEmpty()) {
            seedProducts();
        }
    }

    private void saveProductsToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PRODUCT_FILE))) {
            for (Product p : products.values()) {
                pw.printf("%d;%s;%s;%.2f;%d%n", p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock());
            }
        } catch (IOException e) {
            System.out.println("Gagal menyimpan produk: " + e.getMessage());
        }
    }

    private void addProductInternal(Product p, boolean persist) {
        products.put(p.getId(), p);
        if (persist) saveProductsToFile();
    }

    // ---------------- persistence users ----------------
    private void loadUsers() {
        File f = new File(USER_FILE);
        if (!f.exists()) {
            try { f.createNewFile(); } catch (IOException ignored) {}
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(";");
                if (s.length != 3) continue;
                String username = s[0], pwd = s[1], role = s[2];
                if (role.equals("admin")) users.put(username, new Admin(username, pwd));
                else users.put(username, new Customer(username, pwd));
            }
        } catch (IOException e) {
            System.out.println("Gagal load users: " + e.getMessage());
        }
    }

    public synchronized void saveUserToFile(String username, String password, String role) {
        try (FileWriter fw = new FileWriter(USER_FILE, true)) {
            fw.write(String.format("%s;%s;%s%n", username, password, role));
        } catch (IOException e) {
            System.out.println("Gagal menyimpan user: " + e.getMessage());
        }
    }

    // ---------------- user/product/order logic ----------------
    public void addUser(User u) { users.put(u.getUsername(), u); }
    public User getUserByUsername(String username) { return users.get(username); }
    public List<User> getUsersSnapshot() { return new ArrayList<>(users.values()); }

    public void addProduct(Product p) { addProductInternal(p, true); }
    public boolean removeProductById(int id) {
        boolean removed = products.remove(id) != null;
        if (removed) saveProductsToFile();
        return removed;
    }
    public Product getProductById(int id) { return products.get(id); }
    public Order getOrderById(int id) {
        for (Order o : orders) {
            if (o.getId() == id) return o;
        }
        return null;
    }
    public List<Order> getOrdersByCustomer(Customer customer) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getCustomer().getUsername().equals(customer.getUsername())) {
                result.add(order);
            }
        }
        return result;
    }
    public List<Product> getProductsSnapshot() { return new ArrayList<>(products.values()); }
    public void listProducts() {
        System.out.println("\n-- DAFTAR PRODUK --");
        if (products.isEmpty()) { System.out.println("Belum ada produk."); return; }
        System.out.println(TablePrinter.renderProducts(products.values()));
    }

    public Order createOrder(Customer customer, Cart cart, PaymentMethod pm, TransferBank transferBank) {
        if (pm == PaymentMethod.TRANSFER_BANK && transferBank == null) {
            throw new IllegalArgumentException("Transfer bank wajib memilih bank tujuan.");
        }
        // check stock
        for (CartItem ci : cart.getItems()) {
            Product p = products.get(ci.getProduct().getId());
            if (p == null || p.getStock() < ci.getQuantity()) {
                // return null to indicate failure
                return null;
            }
        }
        // reduce stock
        for (CartItem ci : cart.getItems()) {
            Product p = products.get(ci.getProduct().getId());
            p.reduceStock(ci.getQuantity());
        }
        Order o = new Order(customer, cart.getItems(), cart.totalAmount(), pm, transferBank);
        orders.add(o);
        saveProductsToFile();
        return o;
    }

    public void listOrders() {
        System.out.println("\n-- DAFTAR ORDER --");
        if (orders.isEmpty()) { System.out.println("Belum ada order."); return; }
        System.out.println(TablePrinter.renderOrders(orders));
    }

    public void printCustomerOrderHistory(Customer customer) {
        System.out.println("\n-- PESANAN ANDA --");
        List<Order> customerOrders = getOrdersByCustomer(customer);
        if (customerOrders.isEmpty()) { System.out.println("Belum ada pesanan."); return; }
        System.out.println(TablePrinter.renderOrders(customerOrders));
    }

    public void printAdminOrderHistory() {
        System.out.println("\n-- RIWAYAT ORDER TOKO --");
        if (orders.isEmpty()) { System.out.println("Belum ada riwayat order."); return; }
        System.out.println(TablePrinter.renderOrders(orders));
    }

    public boolean updateOrderStatus(int id, Order.Status status) {
        for (Order o : orders) {
            if (o.getId() == id) {
                if (o.getStatus() != Order.Status.APPROVED && status != Order.Status.APPROVED) {
                    System.out.println("Order harus disetujui terlebih dahulu sebelum mengubah ke status lain.");
                    return false;
                }
                o.setStatus(status);
                System.out.println("Status order " + id + " diubah menjadi " + status + ".");
                if (status == Order.Status.APPROVED) {
                    System.out.println("Invoice:");
                    System.out.println(new Invoice(o));
                }
                return true;
            }
        }
        return false;
    }

    public List<Order> getOrdersSnapshot() { return new ArrayList<>(orders); }

    public void persistProducts() { saveProductsToFile(); }
}
