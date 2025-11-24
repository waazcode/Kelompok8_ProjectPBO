package Kelompok8;

import java.util.Scanner;

public class Customer extends User implements Purchasable {

    private final Cart cart = new Cart();

    public Customer(String username, String password) {
        super(username, password, "customer");
    }

    public Cart getCart() { return cart; }

    @Override
    public void showMenu(Store store, Scanner sc, AuthService auth) {
        System.out.println("\n=== MENU PELANGGAN (" + getUsername() + ") ===");
        while (true) {
            System.out.println("1. Lihat produk");
            System.out.println("2. Tambah ke keranjang");
            System.out.println("3. Lihat keranjang");
            System.out.println("4. Checkout");
            System.out.println("5. Pesanan Saya");
            System.out.println("0. Logout");
            System.out.print("Pilih: ");
            String opt = sc.nextLine().trim();
            try {
                switch (opt) {
                    case "1":
                        store.listProducts();
                        break;
                    case "2":
                        store.listProducts();
                        System.out.print("Masukkan ID produk: ");
                        int id = Integer.parseInt(sc.nextLine().trim());
                        Product p = store.getProductById(id);
                        if (p == null) { System.out.println("Produk tidak ditemukan."); break; }
                        System.out.print("Jumlah: ");
                        int qty = Integer.parseInt(sc.nextLine().trim());
                        if (qty > p.getStock()) {
                            System.out.println("Jumlah melebihi stok tersedia (" + p.getStock() + ").");
                            break;
                        }
                        cart.addItem(p, qty);
                        System.out.println("Ditambahkan ke keranjang.");
                        break;
                    case "3":
                        cart.printCart();
                        break;
                    case "4":
                        if (cart.isEmpty()) { System.out.println("Keranjang kosong."); break; }
                        System.out.println("Pilih metode pembayaran: 1.COD 2.TRANSFER BANK 3.QRIS");
                        int choice = Integer.parseInt(sc.nextLine().trim());
                        PaymentMethod pm = PaymentMethod.values()[Math.max(0, Math.min(PaymentMethod.values().length - 1, choice - 1))];
                        TransferBank transferBank = null;
                        if (pm == PaymentMethod.TRANSFER_BANK) {
                            transferBank = promptTransferBank(sc);
                        }
                        try {
                            Order o = placeOrder(cart, store, pm, transferBank); // interface method
                            if (o != null) {
                                System.out.println("Order dibuat ID: " + o.getId() + ". Menunggu konfirmasi admin.");
                                cart.getItems().clear(); // clear cart after order created
                            } else {
                                System.out.println("Order gagal dibuat.");
                            }
                        } catch (InsufficientStockException ise) {
                            System.out.println("Order gagal: " + ise.getMessage());
                        }
                        break;
                    case "5":
                        store.printCustomerOrderHistory(this);
                        break;
                    case "0":
                        System.out.println("Logout pelanggan...");
                        return;
                    default:
                        System.out.println("Pilihan tidak valid.");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Input angka tidak valid.");
            } catch (Exception e) {
                System.out.println("Terjadi error: " + e.getMessage());
            }
        }
    }

    @Override
    public Order placeOrder(Cart cart, Store store, PaymentMethod pm, TransferBank transferBank)
            throws InsufficientStockException {
        // Implementation delegates to store, which will check stock and may throw InsufficientStockException
        Order o = store.createOrder(this, cart, pm, transferBank);
        if (o == null) throw new InsufficientStockException("Stok tidak mencukupi untuk beberapa item.");
        return o;
    }

    private TransferBank promptTransferBank(Scanner sc) {
        TransferBank[] banks = TransferBank.values();
        while (true) {
            System.out.println("Pilih bank transfer:");
            for (int i = 0; i < banks.length; i++) {
                System.out.println((i + 1) + ". " + banks[i].getLabel());
            }
            System.out.print("Masukkan pilihan bank: ");
            String input = sc.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= banks.length) {
                    return banks[choice - 1];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Pilihan bank tidak valid. Coba lagi.");
        }
    }
}
