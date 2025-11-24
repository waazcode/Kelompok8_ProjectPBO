package Kelompok8;

import java.time.LocalDateTime;

public class Invoice {
    private final Order order;
    private final LocalDateTime printedAt = LocalDateTime.now();

    public Invoice(Order order) { this.order = order; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== STRUK BELANJA ===\n");
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("User: ").append(order.getCustomer().getUsername()).append("\n");
        sb.append("Waktu: ").append(order.getCreatedAt()).append("\n");
        sb.append("Items:\n");
        order.getItems().forEach(i -> sb.append("- ").append(i.toString()).append("\n"));
        sb.append(String.format("Total: Rp %.0f\n", order.totalAmount()));
        sb.append("Metode: ").append(order.getPaymentLabel()).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("Printed at: ").append(printedAt).append("\n");
        sb.append("===============");
        return sb.toString();
    }
}
