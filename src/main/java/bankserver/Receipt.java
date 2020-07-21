package bankserver;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Receipt {
    private static Map<Integer, Receipt> allReceipts = new HashMap<>();
    private ReceiptType receiptType;
    private long money;
    private int sourceAccountID;
    private int destAccountID;
    private String description;
    private int id;
    private boolean paid;

    public Receipt(ReceiptType type, long money, int sourceId, int destId, String description) {
        this.receiptType = type;
        this.money = money;
        this.sourceAccountID = sourceId;
        this.destAccountID = destId;
        this.description = description;
        id = allReceipts.size() + 1;
        allReceipts.put(id, this);
        paid = false;
    }

    public void execute() throws InsufficientBalanceException, InvalidAccountIdException {
        Account source = Account.getAccount(sourceAccountID);
        Account dest = Account.getAccount(destAccountID);
        long amountFromSource = 0;
        long amountToDest = 0;
        if (receiptType == ReceiptType.WITHDRAW || receiptType == ReceiptType.MOVE) {
            if (source == null)
                throw new InvalidAccountIdException();
            amountFromSource = -money;
        }
        if (receiptType == ReceiptType.DEPOSIT || receiptType == ReceiptType.MOVE) {
            if (dest == null)
                throw new InvalidAccountIdException();
            amountToDest = money;
        }
        source.alterBalance(amountFromSource);
        dest.alterBalance(amountToDest);
        paid = true;
    }

    public int getId() {
        return id;
    }

    public int getSourceAccountID() {
        return sourceAccountID;
    }

    public int getDestAccountID() {
        return destAccountID;
    }

    public static Receipt getReceipt(int receiptId) {
        return allReceipts.getOrDefault(receiptId, null);
    }

    public boolean isPaid() {
        return paid;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
