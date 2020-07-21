package bankserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Account {
    private static Map<String, Account> allAccounts = new HashMap<>();
    private static Map<Integer, Account> allAccountsById = new HashMap<>();
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Token token;
    private int id;
    private long balance;
    private ArrayList<Receipt> receiptsWithThisAsTheSource;
    private ArrayList<Receipt> receiptsWithThisAsTheDest;

    public Account(String firstName, String lastName, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        allAccounts.put(username, this);
        id = allAccountsById.size() + 1;
        allAccountsById.put(id, this);
        receiptsWithThisAsTheSource = new ArrayList<>();
        receiptsWithThisAsTheDest = new ArrayList<>();
        balance = 0;
    }

    public static boolean accountExists(String username) {
        return allAccounts.containsKey(username);
    }

    public static Account getAccount(String username, String password) throws InvalidPasswordException {
        Account result = allAccounts.get(username);
        if (result.passwordMatches(password)) {
            return result;
        }
        throw new InvalidPasswordException();
    }

    public static Account getAccount(int id) {
        return allAccountsById.getOrDefault(id, null);
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

    public void alterBalance(long amount) throws InsufficientBalanceException {
        if (balance + amount < 0)
            throw new InsufficientBalanceException();
        balance += amount;
    }

    public void acceptReceipt(Receipt receipt) {
        if (receipt.getSourceAccountID() == id)
            receiptsWithThisAsTheSource.add(receipt);
        if (receipt.getDestAccountID() == id)
            receiptsWithThisAsTheDest.add(receipt);
    }
}
