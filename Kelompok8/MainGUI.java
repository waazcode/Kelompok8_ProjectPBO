package Kelompok8;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GUI sederhana berbasis Swing tanpa mengubah struktur logika inti aplikasi.
 * Panel login/registrasi memanfaatkan Store + AuthService yang sudah ada.
 * Setelah login, panel akan menyesuaikan role (customer/admin) dengan fitur masing-masing.
 */
public class MainGUI extends JFrame {

    private final Store store = new Store();
    private final AuthService auth = new AuthService(store);

    private Customer activeCustomer;
    private Admin activeAdmin;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JPanel customerPanel;
    private final JPanel adminPanel;

    private final DefaultTableModel productTableModel = new DefaultTableModel(
            new Object[]{"ID", "Nama", "Harga", "Stok"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel cartTableModel = new DefaultTableModel(
            new Object[]{"Pilih", "Produk", "Qty", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : Object.class;
        }
    };

    private final DefaultTableModel orderTableModel = new DefaultTableModel(
            new Object[]{"ID", "Customer", "Total", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel orderItemsTableModel = new DefaultTableModel(
            new Object[]{"Produk", "Jumlah", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel historyTableModel = new DefaultTableModel(
            new Object[]{"ID", "Total", "Status", "Metode"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable customerProductTable = new JTable(productTableModel);
    private final JTable adminProductTable = new JTable(productTableModel);
    private final JTable cartTable = new JTable(cartTableModel);
    private final JTable orderTable = new JTable(orderTableModel);
    private final JTable orderItemsTable = new JTable(orderItemsTableModel);
    private final List<CartItem> cartRowItems = new ArrayList<>();

    private final JLabel cartTotalLabel = new JLabel("Total: Rp 0");
    private final JComboBox<PaymentMethod> paymentCombo = new JComboBox<>(PaymentMethod.values());
    private final JComboBox<TransferBank> transferBankCombo = new JComboBox<>(TransferBank.values());

    private final JTextField productIdField = new JTextField();
    private final JTextField productNameField = new JTextField();
    private final JTextField productDescField = new JTextField();
    private final JTextField productPriceField = new JTextField();
    private final JTextField productStockField = new JTextField();

    public MainGUI() {
        super("Anonim Camera Store - GUI");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        JPanel authPanel = buildAuthPanel();
        customerPanel = buildCustomerPanel();
        adminPanel = buildAdminPanel();

        cardPanel.add(authPanel, "AUTH");
        cardPanel.add(customerPanel, "CUSTOMER");
        cardPanel.add(adminPanel, "ADMIN");
        add(cardPanel);

        refreshProducts();
        refreshOrders();
    }

    private JPanel buildAuthPanel() {
        JPanel container = createBasePanel();
        JLabel title = new JLabel("ANONIM CAMERA STORE", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        container.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Registrasi", createRegisterPanel());
        container.add(tabs, BorderLayout.CENTER);
        return container;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 12, 12));
        panel.setBorder(new EmptyBorder(24, 48, 24, 48));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"customer", "admin"});

        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        panel.add(new JLabel("Role"));
        panel.add(roleCombo);

        JButton loginButton = new JButton("Masuk");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = roleCombo.getSelectedItem().toString();
            if (username.isEmpty() || password.isEmpty()) {
                showError("Username dan password wajib diisi.");
                return;
            }
            User user = auth.login(username, password);
            if (user == null) {
                showError("Login gagal. Periksa kembali data Anda.");
                return;
            }
            if (!user.getRole().equals(role)) {
                showError("Role tidak sesuai.");
                return;
            }
            if (user instanceof Customer) {
                activeCustomer = (Customer) user;
                activeAdmin = null;
                switchToCustomer();
            } else if (user instanceof Admin) {
                activeAdmin = (Admin) user;
                activeCustomer = null;
                switchToAdmin();
            } else {
                showError("Role belum didukung.");
            }
        });
        panel.add(loginButton);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 12, 12));
        panel.setBorder(new EmptyBorder(24, 48, 24, 48));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"customer", "admin"});

        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password (min 4 karakter)"));
        panel.add(passwordField);
        panel.add(new JLabel("Role"));
        panel.add(roleCombo);

        JButton registerButton = new JButton("Daftar");
        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = roleCombo.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty()) {
                showError("Lengkapi semua field.");
                return;
            }
            if (username.contains(" ")) {
                showError("Username tidak boleh mengandung spasi.");
                return;
            }
            if (password.length() < 4) {
                showError("Password minimal 4 karakter.");
                return;
            }
            if (store.getUserByUsername(username) != null) {
                showError("Username sudah terdaftar.");
                return;
            }
            User user = role.equals("admin") ? new Admin(username, password) : new Customer(username, password);
            store.addUser(user);
            store.saveUserToFile(username, password, role);
            JOptionPane.showMessageDialog(this, "Registrasi berhasil. Silakan login.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            usernameField.setText("");
            passwordField.setText("");
        });
        panel.add(registerButton);
        return panel;
    }

    private JPanel buildCustomerPanel() {
        JPanel container = createBasePanel();

        JPanel top = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Halo, Customer");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 20f));
        top.add(welcomeLabel, BorderLayout.WEST);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            activeCustomer = null;
            cardLayout.show(cardPanel, "AUTH");
        });
        top.add(logout, BorderLayout.EAST);
        container.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.add(buildProductPanel(true));
        center.add(buildCartPanel());
        container.add(center, BorderLayout.CENTER);

        JLabel helper = new JLabel("Tip: pilih produk, atur Jumlah, lalu klik \"Tambah ke Keranjang\".");
        helper.setHorizontalAlignment(SwingConstants.CENTER);
        helper.setBorder(new EmptyBorder(12, 0, 0, 0));
        container.add(helper, BorderLayout.SOUTH);

        customerProductTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerProductTable.setRowHeight(28);
        cartTable.setRowHeight(26);

        // Update welcome label when switch
        container.putClientProperty("welcomeLabel", welcomeLabel);
        return container;
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(createCardBorder("Keranjang Saya"));
        cartTable.setRowHeight(26);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(0, 1, 8, 8));
