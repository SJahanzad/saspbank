package bankserver;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

public class Account {
    private static AccountRecord accountRecord;

    static {
        try {
            File dataDirectory = new File("data");
            if (!dataDirectory.exists())
                dataDirectory.mkdir();
            File records = new File("data/.records.json");
            if (records.createNewFile()) {
                accountRecord = new AccountRecord();
            } else {
                accountRecord = new Gson().fromJson(FileManager.readWholeFile(records), AccountRecord.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Token token;
    private int id;
    private long balance;
    private ArrayList<Receipt> receiptsWithThisAsTheSource;
    private ArrayList<Receipt> receiptsWithThisAsTheDest;

    public Account(String firstName, String lastName, String username, String password) throws DataBaseException {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        accountRecord.addAccount(this);
        FileManager.writeObjectToFileInAddress(accountRecord, "data/.records");
        id = accountRecord.getCount() + 1;
        receiptsWithThisAsTheSource = new ArrayList<>();
        receiptsWithThisAsTheDest = new ArrayList<>();
        balance = 0;
        FileManager.updateAccount(this);
    }

    public static boolean accountExists(String username) throws DataBaseException {
        return FileManager.fileExists(username);
    }

    public static Account getAccount(String username, String password)
            throws InvalidPasswordException, DataBaseException {
        Account result = FileManager.getAccount(username);
        if (result.passwordMatches(password)) {
            return result;
        }
        throw new InvalidPasswordException();
    }

    public static Account getAccount(int id) {
        return accountRecord.getAccountById(id);
    }

    public static Account getAccount(String idString) {
        int id = -1;
        try {
            id = Integer.parseInt(idString);
        } catch (Exception e) {
            return null;
        }
        return getAccount(id);
    }

    public static AccountRecord getAccountRecord() {
        return accountRecord;
    }

    public boolean passwordMatches(String password) {
        return this.password.equals(password);
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        if (token == null || token.hasExpired())
            token = new Token(this);
        return token.toString();
    }

    public String getTransactions(String mode) {
        ArrayList<Receipt> receipts = receiptsWithThisAsTheDest;
        if (mode.equals("-"))
            receipts = receiptsWithThisAsTheSource;
        StringBuilder result = new StringBuilder();
        for (Receipt receipt : receipts) {
            if (result.length() > 0)
                result.append("*");
            result.append(receipt);
        }
        if (mode.equals("*"))
            receipts = receiptsWithThisAsTheSource;
        for (Receipt receipt : receipts) {
            if (result.length() > 0)
                result.append("*");
            result.append(receipt);
        }
        return result.toString();
    }

    public long getBalance() {
        return balance;
    }

    public void alterBalance(long amount) throws InsufficientBalanceException, DataBaseException {
        if (balance + amount < 0)
            throw new InsufficientBalanceException();
        balance += amount;
        FileManager.updateAccount(this);
    }

    public void acceptReceipt(Receipt receipt) throws DataBaseException {
        if (receipt.getSourceAccountID() == id)
            receiptsWithThisAsTheSource.add(receipt);
        if (receipt.getDestAccountID() == id)
            receiptsWithThisAsTheDest.add(receipt);
        FileManager.updateAccount(this);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
