package bankserver;

public enum ReceiptType {
    DEPOSIT, WITHDRAW, MOVE;

    public static ReceiptType getTypeFromString(String string) throws InvalidReceiptTypeException {
        switch (string) {
            case "deposit":
                return DEPOSIT;
            case "withdraw":
                return WITHDRAW;
            case "move":
                return MOVE;
            default:
                throw new InvalidReceiptTypeException();
        }
    }

    @Override
    public String toString() {
        if (this == DEPOSIT)
            return "deposit";
        else if (this == WITHDRAW)
            return "withdraw";
        else
            return "move";
    }
}
