package Kelompok8;

public abstract class User {
    protected String username;
    private String password;
    private final String role; // "admin" atau "customer"

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Encapsulation: password is private and checked via method
    public boolean checkPassword(String pw) {
        return pw != null && pw.equals(this.password);
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }

    // Abstraction + Polymorphism: subclasses must provide a menu
    public abstract void showMenu(Store store, java.util.Scanner sc, AuthService auth);
}
