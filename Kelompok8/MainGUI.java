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
        cartTotalLabel.setFont(cartTotalLabel.getFont().deriveFont(Font.BOLD, 16f));
        bottom.add(cartTotalLabel);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.add(new JLabel("Bayar via:"));
        paymentCombo.setSelectedIndex(0);
        paymentCombo.addActionListener(e -> updateTransferBankState());
        qtyPanel.add(paymentCombo);

        JLabel bankLabel = new JLabel("Bank:");
        transferBankCombo.setEnabled(false);
        qtyPanel.add(bankLabel);
        qtyPanel.add(transferBankCombo);
        updateTransferBankState();
        bottom.add(qtyPanel);

        JButton checkoutBtn = new JButton("Checkout Terpilih");
        checkoutBtn.addActionListener(e -> checkoutSelectedItems());
        JButton clearBtn = new JButton("Kosongkan");
        clearBtn.addActionListener(e -> {
            if (activeCustomer == null) return;
            activeCustomer.getCart().clear();
            refreshCart();
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JButton removeSelectedBtn = new JButton("Hapus Terpilih");
        removeSelectedBtn.addActionListener(e -> removeSelectedCartItems());
        actionPanel.add(removeSelectedBtn);
        actionPanel.add(clearBtn);
        actionPanel.add(checkoutBtn);
        bottom.add(actionPanel);

        JButton historyBtn = new JButton("Riwayat Pesanan");
        historyBtn.addActionListener(e -> showCustomerOrders());
        bottom.add(historyBtn);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildProductPanel(boolean forCustomer) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(createCardBorder("Daftar Produk"));
        JTable table = forCustomer ? customerProductTable : adminProductTable;
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        if (forCustomer) {
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
            JLabel qtyLabel = new JLabel("Jumlah:");
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
            JSpinner qtySpinner = new JSpinner(spinnerModel);
            JButton addButton = new JButton("Tambah ke Keranjang");
            addButton.addActionListener(e -> {
                if (activeCustomer == null) {
                    showError("Silakan login sebagai customer terlebih dahulu.");
                    return;
                }
                int selected = customerProductTable.getSelectedRow();
                if (selected < 0) {
                    showError("Pilih produk terlebih dahulu.");
                    return;
                }
                int productId = (int) productTableModel.getValueAt(selected, 0);
                Product product = store.getProductById(productId);
                if (product == null) {
                    showError("Produk tidak ditemukan.");
                    return;
                }
                int qty = (int) qtySpinner.getValue();
                if (qty <= 0 || qty > product.getStock()) {
                    showError("Jumlah tidak valid atau melebihi stok.");
                    return;
                }
                activeCustomer.getCart().addItem(product, qty);
                refreshCart();
            });
            bottom.add(qtyLabel);
            bottom.add(qtySpinner);
            bottom.add(addButton);
            panel.add(bottom, BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel buildAdminPanel() {
        JPanel container = createBasePanel();

        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Dashboard Admin");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        top.add(title, BorderLayout.WEST);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            activeAdmin = null;
            cardLayout.show(cardPanel, "AUTH");
        });
        top.add(logout, BorderLayout.EAST);
        container.add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setLeftComponent(buildAdminProductArea());
        split.setRightComponent(buildAdminOrderArea());
        container.add(split, BorderLayout.CENTER);

        return container;
    }

    private JPanel buildAdminProductArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(createCardBorder("Kelola Produk"));

        adminProductTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adminProductTable.getSelectionModel().addListSelectionListener(e -> {
            if (adminProductTable.getSelectedRow() >= 0) {
                populateProductForm(adminProductTable.getSelectedRow());
            }
        });
        panel.add(new JScrollPane(adminProductTable), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(new JLabel("ID"));
        form.add(productIdField);
        form.add(new JLabel("Nama"));
        form.add(productNameField);
        form.add(new JLabel("Deskripsi"));
        form.add(productDescField);
        form.add(new JLabel("Harga"));
        form.add(productPriceField);
        form.add(new JLabel("Stok"));
        form.add(productStockField);
        panel.add(form, BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton addBtn = new JButton("Tambah");
        addBtn.addActionListener(e -> addProduct());
        JButton updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> updateProduct());
        JButton deleteBtn = new JButton("Hapus");
        deleteBtn.addActionListener(e -> deleteProduct());

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        panel.add(buttons, BorderLayout.NORTH);

        return panel;
    }

    private JPanel buildAdminOrderArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(createCardBorder("Order Masuk"));
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(26);
        JScrollPane orderScroll = new JScrollPane(orderTable);
        panel.add(orderScroll, BorderLayout.CENTER);

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateOrderItems(orderTable.getSelectedRow());
            }
        });

        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(createCardBorder("Rincian Barang"));
        orderItemsTable.setRowHeight(24);
        detailPanel.add(new JScrollPane(orderItemsTable), BorderLayout.CENTER);
        detailPanel.setPreferredSize(new Dimension(200, 140));
        panel.add(detailPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JComboBox<Order.Status> statusCombo = new JComboBox<>(Order.Status.values());
        JButton updateBtn = new JButton("Perbarui Status");
        updateBtn.addActionListener(e -> {
            int row = orderTable.getSelectedRow();
            if (row < 0) {
                showError("Pilih order terlebih dahulu.");
                return;
            }
            int orderId = (int) orderTableModel.getValueAt(row, 0);
            Order.Status status = (Order.Status) statusCombo.getSelectedItem();
            Order currentOrder = store.getOrderById(orderId);
            if (currentOrder != null && currentOrder.getStatus() != Order.Status.APPROVED && status != Order.Status.APPROVED) {
                showError("Setujui order terlebih dahulu (status APPROVED) sebelum memilih status lain.");
                return;
            }
            boolean updated = store.updateOrderStatus(orderId, status);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Status order diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                if (status == Order.Status.APPROVED) {
                    Order approvedOrder = store.getOrderById(orderId);
                    if (approvedOrder != null) {
                        showInvoiceDialog(approvedOrder);
                    }
                }
                refreshOrders();
            } else {
                showError("Gagal memperbarui status.");
            }
        });
        JButton refreshBtn = new JButton("Segarkan");
        refreshBtn.addActionListener(e -> refreshOrders());

        statusPanel.add(new JLabel("Status"));
        statusPanel.add(statusCombo);
        statusPanel.add(updateBtn);
        statusPanel.add(refreshBtn);
        panel.add(statusPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void switchToCustomer() {
        JLabel welcomeLabel = (JLabel) customerPanel.getClientProperty("welcomeLabel");
        if (welcomeLabel != null && activeCustomer != null) {
            welcomeLabel.setText("Halo, " + activeCustomer.getUsername());
        }
        refreshProducts();
        refreshCart();
        cardLayout.show(cardPanel, "CUSTOMER");
    }

    private void switchToAdmin() {
        refreshProducts();
        refreshOrders();
        cardLayout.show(cardPanel, "ADMIN");
    }

    private void refreshProducts() {
        productTableModel.setRowCount(0);
        for (Product p : store.getProductsSnapshot()) {
            productTableModel.addRow(new Object[]{p.getId(), p.getName(), formatCurrency(p.getPrice()), p.getStock()});
        }
    }

    private void refreshCart() {
        cartTableModel.setRowCount(0);
        cartRowItems.clear();
        double total = 0;
        if (activeCustomer != null) {
            for (CartItem item : activeCustomer.getCart().getItems()) {
                cartTableModel.addRow(new Object[]{
                        Boolean.FALSE,
                        item.getProduct().getName(),
                        item.getQuantity(),
                        formatCurrency(item.subtotal())
                });
                cartRowItems.add(item);
                total += item.subtotal();
            }
        }
        cartTotalLabel.setText("Total: " + formatCurrency(total));
    }

    private void updateTransferBankState() {
        boolean enabled = paymentCombo.getSelectedItem() == PaymentMethod.TRANSFER_BANK;
        transferBankCombo.setEnabled(enabled);
    }

  private void refreshOrders() {
        orderTableModel.setRowCount(0);
        for (Order o : store.getOrdersSnapshot()) {
            orderTableModel.addRow(new Object[]{
                    o.getId(),
                    o.getCustomer().getUsername(),
                    formatCurrency(o.totalAmount()),
                    o.getStatus()
            });
        }
        if (orderTableModel.getRowCount() > 0) {
            orderTable.setRowSelectionInterval(0, 0);
            populateOrderItems(0);
        } else {
            orderItemsTableModel.setRowCount(0);
        }
    }

    private void checkoutSelectedItems() {
        if (activeCustomer == null) return;
        List<CartItem> selectedItems = getSelectedCartItems();
        if (selectedItems.isEmpty()) {
            showError("Pilih minimal satu produk untuk checkout.");
            return;
        }
        Cart tempCart = new Cart();
        List<Integer> selectedProductIds = new ArrayList<>();
        for (CartItem item : selectedItems) {
            tempCart.addItem(item.getProduct(), item.getQuantity());
            selectedProductIds.add(item.getProduct().getId());
        }
        PaymentMethod pm = (PaymentMethod) paymentCombo.getSelectedItem();
        TransferBank transferBank = null;
        if (pm == PaymentMethod.TRANSFER_BANK) {
            transferBank = (TransferBank) transferBankCombo.getSelectedItem();
            if (transferBank == null) {
                showError("Pilih bank tujuan untuk transfer.");
                return;
            }
        }
        try {
            Order order = activeCustomer.placeOrder(tempCart, store, pm, transferBank);
            if (order != null) {
                JOptionPane.showMessageDialog(this,
                        "Order berhasil dibuat dengan ID #" + order.getId() + ". Menunggu konfirmasi admin.",
                        "Checkout Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);
                activeCustomer.getCart().removeItemsByProductIds(selectedProductIds);
                refreshCart();
                refreshProducts();
                refreshOrders();
            } else {
                showError("Order gagal dibuat. Coba lagi.");
            }
        } catch (InsufficientStockException e) {
            showError("Stok tidak mencukupi: " + e.getMessage());
            refreshProducts();
        }
    }

    private List<CartItem> getSelectedCartItems() {
        List<CartItem> selected = new ArrayList<>();
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            Object value = cartTableModel.getValueAt(i, 0);
            boolean isSelected = value instanceof Boolean && (Boolean) value;
            if (isSelected && i < cartRowItems.size()) {
                selected.add(cartRowItems.get(i));
            }
        }
        return selected;
    }

    private void removeSelectedCartItems() {
        if (activeCustomer == null) return;
        List<CartItem> selectedItems = getSelectedCartItems();
        if (selectedItems.isEmpty()) {
            showError("Pilih minimal satu produk untuk dibatalkan.");
            return;
        }
        List<Integer> ids = new ArrayList<>();
        for (CartItem item : selectedItems) {
            ids.add(item.getProduct().getId());
        }
        activeCustomer.getCart().removeItemsByProductIds(ids);
        refreshCart();
    }

    private void showCustomerOrders() {
        if (activeCustomer == null) return;
        historyTableModel.setRowCount(0);
        for (Order o : store.getOrdersByCustomer(activeCustomer)) {
            historyTableModel.addRow(new Object[]{
                    o.getId(),
                    formatCurrency(o.totalAmount()),
                    o.getStatus(),
                    o.getPaymentLabel()
            });
        }

        JTable historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(26);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(520, 240));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(scrollPane, BorderLayout.CENTER);

        if (historyTableModel.getRowCount() == 0) {
            panel.add(new JLabel("Belum ada pesanan."), BorderLayout.SOUTH);
        }

        JOptionPane.showMessageDialog(this, panel, "Riwayat Pesanan", JOptionPane.PLAIN_MESSAGE);
    }

    private void populateOrderItems(int orderRow) {
        orderItemsTableModel.setRowCount(0);
        if (orderRow < 0 || orderRow >= orderTableModel.getRowCount()) return;
        int orderId = (int) orderTableModel.getValueAt(orderRow, 0);
        Order order = store.getOrderById(orderId);
        if (order == null) return;
        for (CartItem item : order.getItems()) {
            orderItemsTableModel.addRow(new Object[]{
                    item.getProduct().getName(),
                    item.getQuantity(),
                    formatCurrency(item.subtotal())
            });
        }
    }

    private void showInvoiceDialog(Order order) {
        Invoice invoice = new Invoice(order);
        JTextArea textArea = new JTextArea(invoice.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(440, 320));

        JOptionPane.showMessageDialog(this, scrollPane, "Invoice Order #" + order.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void populateProductForm(int row) {
        if (row < 0) return;
        Object idObj = productTableModel.getValueAt(row, 0);
        productIdField.setText(String.valueOf(idObj));
        productNameField.setText(String.valueOf(productTableModel.getValueAt(row, 1)));
        // Deskripsi tidak ada di tabel; ambil langsung dari store
        Product p = store.getProductById((int) idObj);
        if (p != null) {
            productDescField.setText(p.getDescription());
            productPriceField.setText(String.valueOf((long) p.getPrice()));
            productStockField.setText(String.valueOf(p.getStock()));
        }
    }

    private void addProduct() {
        try {
            int id = Integer.parseInt(productIdField.getText().trim());
            String name = productNameField.getText().trim();
            String desc = productDescField.getText().trim();
            double price = Double.parseDouble(productPriceField.getText().trim());
            int stock = Integer.parseInt(productStockField.getText().trim());
            if (name.isEmpty() || desc.isEmpty()) {
                showError("Nama dan deskripsi wajib diisi.");
                return;
            }
            if (store.getProductById(id) != null) {
                showError("ID produk sudah dipakai.");
                return;
            }
            store.addProduct(new Product(id, name, desc, price, stock));
            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            refreshProducts();
            clearProductForm();
        } catch (NumberFormatException ex) {
            showError("Pastikan ID, harga, dan stok berupa angka.");
        }
    }

    private void updateProduct() {
        try {
            int id = Integer.parseInt(productIdField.getText().trim());
            Product product = store.getProductById(id);
            if (product == null) {
                showError("Produk tidak ditemukan.");
                return;
            }
            if (!productNameField.getText().trim().isEmpty()) {
                product.setName(productNameField.getText().trim());
            }
            if (!productDescField.getText().trim().isEmpty()) {
                product.setDescription(productDescField.getText().trim());
            }
            if (!productPriceField.getText().trim().isEmpty()) {
                product.setPrice(Double.parseDouble(productPriceField.getText().trim()));
            }
            if (!productStockField.getText().trim().isEmpty()) {
                product.setStock(Integer.parseInt(productStockField.getText().trim()));
            }
            store.persistProducts();
            JOptionPane.showMessageDialog(this, "Produk diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            refreshProducts();
        } catch (NumberFormatException ex) {
            showError("Input angka tidak valid.");
        }
    }

    private void deleteProduct() {
        try {
            int id = Integer.parseInt(productIdField.getText().trim());
            boolean removed = store.removeProductById(id);
            if (removed) {
                JOptionPane.showMessageDialog(this, "Produk dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                refreshProducts();
                clearProductForm();
            } else {
                showError("Produk tidak ditemukan.");
            }
        } catch (NumberFormatException ex) {
            showError("Masukkan ID angka sebelum menghapus.");
        }
    }

    private void clearProductForm() {
        productIdField.setText("");
        productNameField.setText("");
        productDescField.setText("");
        productPriceField.setText("");
        productStockField.setText("");
    }

    private JPanel createBasePanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setBackground(new Color(245, 246, 250));
        return panel;
    }

    private TitledBorder createCardBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
        return border;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }

    private String formatCurrency(double amount) {
        return String.format(new Locale("id", "ID"), "Rp %,.0f", amount);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}

