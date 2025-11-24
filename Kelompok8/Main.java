package Kelompok8;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Store store = new Store();
        AuthService auth = new AuthService(store);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== ANONIM CAMERA STORE ===");

        while (true) {
            System.out.println("\nMenu Utama:");
            System.out.println("1. Daftar akun");
            System.out.println("2. Login");
            System.out.println("0. Keluar");
            System.out.print("Pilih: ");
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1":
                    registerFlow(store, sc);
                    break;
                case "2":
                    loginFlow(store, auth, sc);
                    break;
                case "0":
                    System.out.println("Sampai jumpa!");
                    sc.close();
                    return;
                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private static void registerFlow(Store store, Scanner sc) {
        System.out.println("\n=== Daftar Akun ===");
        System.out.println("Pilih role:");
        System.out.println("1. Customer");
        System.out.println("2. Admin");
        System.out.print("Pilihan: ");
        String role = sc.nextLine().trim();
        String roleName;
        if (role.equals("1")) roleName = "customer";
        else if (role.equals("2")) roleName = "admin";
        else { System.out.println("Pilihan role tidak valid."); return; }

        System.out.print("Masukkan username: ");
        String username = sc.nextLine().trim();
        if (username.contains(" ")) { System.out.println("Username tidak boleh mengandung spasi."); return; }
        if (store.getUserByUsername(username) != null) { System.out.println("Username sudah terdaftar."); return; }

        System.out.print("Masukkan password (min 4 char): ");
        String password = sc.nextLine().trim();
        if (password.length() < 4) { System.out.println("Password terlalu pendek."); return; }

        User u;
        if (roleName.equals("admin")) u = new Admin(username, password);
        else u = new Customer(username, password);

        store.addUser(u);
        store.saveUserToFile(username, password, roleName);
        System.out.println("Pendaftaran berhasil. Silakan login.");
    }

    private static void loginFlow(Store store, AuthService auth, Scanner sc) {
        System.out.println("\n=== Login ===");
        System.out.println("Pilih role:");
        System.out.println("1. Customer");
        System.out.println("2. Admin");
        System.out.print("Pilihan: ");
        String role = sc.nextLine().trim();
        String roleName;
        if (role.equals("1")) roleName = "customer";
        else if (role.equals("2")) roleName = "admin";
        else { System.out.println("Pilihan role tidak valid."); return; }

        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        User u = auth.login(username, password);
        if (u == null) { System.out.println("Login gagal: username/password salah."); return; }
        if (!u.getRole().equals(roleName)) { System.out.println("Role tidak sesuai."); return; }

        // Polymorphism: call showMenu on User reference
        u.showMenu(store, sc, auth);
    }
}
