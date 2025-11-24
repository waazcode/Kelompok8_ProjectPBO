package Kelompok8;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    public enum Status { PENDING, APPROVED, SENDING, REJECTED }

    private static int COUNTER = 1000;
    private final int id;
    private final Customer customer;
    private final List<CartItem> items;
    private final double total;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime createdAt;
    private final TransferBank transferBank;
    private Status status = Status.PENDING;

    public Order(Customer customer, List<CartItem> items, double total, PaymentMethod pm, TransferBank transferBank) {
        this.id = COUNTER++;
        this.customer = customer;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.paymentMethod = pm;
        this.transferBank = transferBank;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<CartItem> getItems() { return new ArrayList<>(items); }
    public double totalAmount() { return total; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public TransferBank getTransferBank() { return transferBank; }
    public String getPaymentLabel() {
        if (paymentMethod == PaymentMethod.TRANSFER_BANK && transferBank != null) {
            return paymentMethod + " - " + transferBank.getLabel();
        }
        return paymentMethod.toString();
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public boolean isApproved() { return status == Status.APPROVED; }

    public void approve() { this.status = Status.APPROVED; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Order ID:%d | user:%s | Rp %.0f | %s | %s",
                id, customer.getUsername(), total, getPaymentLabel(), status);
    }
}
