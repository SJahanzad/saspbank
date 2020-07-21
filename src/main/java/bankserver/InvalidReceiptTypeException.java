package bankserver;

public class InvalidReceiptTypeException extends Exception {
    public InvalidReceiptTypeException() {
        super("invalid receipt type");
    }
}
