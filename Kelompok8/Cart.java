package Kelompok8;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product p, int qty) {
        for (CartItem it : items) {
            if (it.getProduct().getId() == p.getId()) {
                it.setQuantity(it.getQuantity() + qty);
                return;
            }
        }
        items.add(new CartItem(p, qty));
    }

    public List<CartItem> getItems() { return items; }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }

    public void removeItemsByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return;
        Set<Integer> targetIds = new HashSet<>(productIds);
        items.removeIf(ci -> targetIds.contains(ci.getProduct().getId()));
    }

    public double totalAmount() {
        return items.stream().mapToDouble(CartItem::subtotal).sum();
    }

    public void printCart() {
        System.out.println("\n--- Keranjang ---");
        if (items.isEmpty()) { System.out.println("Kosong."); return; }
        System.out.println(TablePrinter.renderCartItems(items));
        System.out.printf("Total: Rp %.0f%n", totalAmount());
    }
}
