package bankserver;

public class InvalidPasswordException extends Exception {
    public InvalidPasswordException() {
        super("invalid password");
    }
}
