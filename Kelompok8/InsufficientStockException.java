package Kelompok8;

/**
 * Custom checked exception to indicate stock problems.
 */
public class InsufficientStockException extends Exception {
    public InsufficientStockException(String message) { super(message); }
}
