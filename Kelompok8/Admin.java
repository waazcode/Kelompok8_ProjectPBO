package Kelompok8;

import java.util.Scanner;

public class Admin extends User {

    public Admin(String username, String password) {
        super(username, password, "admin");
    }

    @Override
    public void showMenu(Store store, Scanner sc, AuthService auth) {
        System.out.println("\n=== MENU ADMIN ===");
        while (true) {
            System.out.println("1. Tambah produk");
            System.out.println("2. Edit produk");
            System.out.println("3. Hapus produk");
            System.out.println("4. Lihat produk");
            System.out.println("5. Lihat order");
            System.out.println("6. Ubah status order");
            System.out.println("7. Riwayat order");
            System.out.println("0. Logout");
            System.out.print("Pilih: ");
            String opt = sc.nextLine().trim();
            try {
                switch (opt) {
                    case "1": // tambah
                        System.out.print("ID (angka unik): ");
                        int id = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Nama: ");
                        String name = sc.nextLine().trim();
                        System.out.print("Deskripsi: ");
                        String desc = sc.nextLine().trim();
                        System.out.print("Harga: ");
                        double price = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("Stok: ");
                        int stock = Integer.parseInt(sc.nextLine().trim());
                        store.addProduct(new Product(id, name, desc, price, stock));
                        System.out.println("Produk ditambahkan.");
                        break;
                    case "2": // edit
                        store.listProducts();
                        System.out.print("ID produk yang diedit: ");
                        int eid = Integer.parseInt(sc.nextLine().trim());
                        Product ep = store.getProductById(eid);
                        if (ep == null) { System.out.println("Produk tidak ditemukan."); break; }
                        System.out.print("Nama baru (enter = skip): ");
                        String nn = sc.nextLine();
                        if (!nn.isBlank()) ep.setName(nn.trim());
                        System.out.print("Deskripsi baru (enter = skip): ");
                        String nd = sc.nextLine();
                        if (!nd.isBlank()) ep.setDescription(nd.trim());
                        System.out.print("Harga baru (enter = skip): ");
                        String ph = sc.nextLine();
                        if (!ph.isBlank()) ep.setPrice(Double.parseDouble(ph.trim()));
                        System.out.print("Stok baru (enter = skip): ");
                        String st = sc.nextLine();
                        if (!st.isBlank()) ep.setStock(Integer.parseInt(st.trim()));
                        store.persistProducts();
                        System.out.println("Produk diupdate.");
                        break;
                    case "3":
                        store.listProducts();
                        System.out.print("ID produk yang dihapus: ");
                        int hid = Integer.parseInt(sc.nextLine().trim());
                        boolean removed = store.removeProductById(hid);
                        System.out.println(removed ? "Produk dihapus." : "Produk tidak ditemukan.");
                        break;
                    case "4":
                        store.listProducts();
                        break;
                    case "5":
                        store.listOrders();
                        break;
                    case "6":
                        store.listOrders();
                        System.out.print("Masukkan ID order: ");
                        int oid = Integer.parseInt(sc.nextLine().trim());
                        System.out.println("Pilih status baru: 1. PENDING 2. APPROVED 3. SENDING");
                        String statusOpt = sc.nextLine().trim();
                        Order.Status status = null;
                        switch (statusOpt) {
                            case "1": status = Order.Status.PENDING; break;
                            case "2": status = Order.Status.APPROVED; break;
                            case "3": status = Order.Status.SENDING; break;
                            default:
                                System.out.println("Pilihan status tidak valid.");
                        }
                        if (status != null) {
                            boolean updated = store.updateOrderStatus(oid, status);
                            System.out.println(updated ? "Status order diperbarui." : "Gagal mengubah status (cek ID).");
                        }
                        break;
                    case "7":
                        store.printAdminOrderHistory();
                        break;
                    case "0":
                        System.out.println("Logout admin...");
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
}
