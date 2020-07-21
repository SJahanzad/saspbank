package bankserver;

public class InvalidTokenException extends Exception {
    public InvalidTokenException() {
        super("token is invalid");
    }
}
