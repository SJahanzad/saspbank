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
            File records = new File(FileManager.getJsonFileAddress(".records"));
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
    private String id;
    private long balance;
    private ArrayList<Receipt> receiptsWithThisAsTheSource;
    private ArrayList<Receipt> receiptsWithThisAsTheDest;

    public Account(String firstName, String lastName, String username, String password) throws DataBaseException {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        balance = 0;
        id = DataManager.getNewId();
        accountRecord.addAccount(this);
        FileManager.writeObjectToJsonFileWithName(accountRecord, ".records");
        receiptsWithThisAsTheSource = new ArrayList<>();
        receiptsWithThisAsTheDest = new ArrayList<>();
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

    public static Account getAccount(String id) {
        return accountRecord.getAccountById(id);
    }

    public static AccountRecord getAccountRecord() {
        return accountRecord;
    }

    public boolean passwordMatches(String password) {
        return this.password.equals(password);
    }

    public String getId() {
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
        if (receipt.getSourceAccountID().equals(id))
            receiptsWithThisAsTheSource.add(receipt);
        if (receipt.getDestAccountID().equals(id))
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
