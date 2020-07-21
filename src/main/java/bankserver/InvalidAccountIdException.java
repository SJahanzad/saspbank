package bankserver;

public class InvalidAccountIdException extends Exception {
    public InvalidAccountIdException() {
        super("invalid account id");
    }
}
