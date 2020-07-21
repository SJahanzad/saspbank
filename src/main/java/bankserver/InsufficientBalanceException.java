package bankserver;

public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException() {
        super("source account does not have enough money");
    }
}
