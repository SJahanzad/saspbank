package bankserver;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Receipt {
    private static Map<String, Receipt> allReceipts = new HashMap<>();
    private ReceiptType receiptType;
    private long money;
    private String sourceAccountID;
    private String destAccountID;
    private String description;
    private String id;
    private boolean paid;

    public Receipt(ReceiptType type, long money, String sourceId, String destId, String description) {
        this.receiptType = type;
        this.money = money;
        this.sourceAccountID = sourceId;
        this.destAccountID = destId;
        this.description = description;
        id = DataManager.getNewId();
        paid = false;
        allReceipts.put(id, this);
    }

    public void execute() throws InsufficientBalanceException, InvalidAccountIdException, DataBaseException {
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
        if (source != null) source.alterBalance(amountFromSource);
        if (dest != null) dest.alterBalance(amountToDest);
        paid = true;
    }

    public String getId() {
        return id;
    }

    public String getSourceAccountID() {
        return sourceAccountID;
    }

    public String getDestAccountID() {
        return destAccountID;
    }

    public static Receipt getReceipt(String receiptId) {
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
