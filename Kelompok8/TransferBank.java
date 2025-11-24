package Kelompok8;

/**
 * Opsional bank yang dapat dipilih ketika pembayaran via transfer bank.
 */
public enum TransferBank {
    BCA("Bank BCA"),
    BNI("Bank BNI"),
    BRI("Bank BRI"),
    MANDIRI("Bank Mandiri");

    private final String label;

    TransferBank(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}

