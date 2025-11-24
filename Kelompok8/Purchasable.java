package Kelompok8;

/**
 * Interface sebagai contoh penggunaan Abstraction via Interface.
 */
public interface Purchasable {
    Order placeOrder(Cart cart, Store store, PaymentMethod pm, TransferBank transferBank)
            throws InsufficientStockException;
}
