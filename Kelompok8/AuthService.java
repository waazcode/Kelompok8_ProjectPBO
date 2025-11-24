package Kelompok8;

public class AuthService {
    private final Store store;

    public AuthService(Store store) { this.store = store; }

    public User login(String username, String password) {
        User u = store.getUserByUsername(username);
        if (u != null && u.checkPassword(password)) return u;
        return null;
    }
}
