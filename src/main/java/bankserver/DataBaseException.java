package bankserver;

public class DataBaseException extends Exception {
    public DataBaseException() {
        super("database error");
    }
}
