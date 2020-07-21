package bankserver;

public class ExpiredTokenException extends Exception {
    public ExpiredTokenException() {
        super("token expired");
    }
}
