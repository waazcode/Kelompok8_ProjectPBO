package Kelompok8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TablePrinter {
    private TablePrinter() {}

    public static String renderProducts(Collection<Product> products) {
        String[] headers = {"ID", "Nama", "Harga", "Stok", "Deskripsi"};
        List<String[]> rows = new ArrayList<>();
        for (Product product : products) {
            rows.add(new String[]{
                    String.valueOf(product.getId()),
                    product.getName(),
                    formatCurrency(product.getPrice()),
                    String.valueOf(product.getStock()),
                    product.getDescription()
            });
        }
        return buildTable(headers, rows);
    }

    public static String renderCartItems(Collection<CartItem> items) {
        String[] headers = {"Produk", "Jumlah", "Harga Satuan", "Subtotal"};
        List<String[]> rows = new ArrayList<>();
        for (CartItem item : items) {
            rows.add(new String[]{
                    item.getProduct().getName(),
                    String.valueOf(item.getQuantity()),
                    formatCurrency(item.getProduct().getPrice()),
                    formatCurrency(item.subtotal())
            });
        }
        return buildTable(headers, rows);
    }

    public static String renderOrders(Collection<Order> orders) {
        String[] headers = {"ID", "Customer", "Total", "Metode", "Status", "Waktu"};
        List<String[]> rows = new ArrayList<>();
        for (Order order : orders) {
            rows.add(new String[]{
                    String.valueOf(order.getId()),
                    order.getCustomer().getUsername(),
                    formatCurrency(order.totalAmount()),
                    order.getPaymentLabel(),
                    order.getStatus().name(),
                    order.getCreatedAt().toString()
            });
        }
        return buildTable(headers, rows);
    }

    private static String buildTable(String[] headers, List<String[]> rows) {
        int columnCount = headers.length;
        int[] widths = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < columnCount; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }
        for (int i = 0; i < columnCount; i++) {
            widths[i] += 2; // padding
        }

        StringBuilder sb = new StringBuilder();
        sb.append(drawSeparator(widths)).append("\n");
        sb.append(drawRow(headers, widths)).append("\n");
        sb.append(drawSeparator(widths)).append("\n");
        for (String[] row : rows) {
            sb.append(drawRow(row, widths)).append("\n");
        }
        sb.append(drawSeparator(widths));
        return sb.toString();
    }

    private static String drawSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append('+');
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append('+');
        }
        return sb.toString();
    }

    private static String drawRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append('|');
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            sb.append(' ').append(pad(value, widths[i])).append(' ').append('|');
        }
        return sb.toString();
    }

    private static String pad(String value, int width) {
        if (value.length() >= width) return value;
        return value + " ".repeat(width - value.length());
    }

    private static String formatCurrency(double value) {
        return String.format("Rp %,.0f", value).replace(',', '.');
    }
}

