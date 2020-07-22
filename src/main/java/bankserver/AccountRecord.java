package bankserver;

import java.util.HashMap;
import java.util.Map;

public class AccountRecord {
    private int count;
    private Map<Integer, Account> allAccountsById;

    public AccountRecord() {
        count = 0;
        allAccountsById = new HashMap<>();
    }

    public int getCount() {
        return count;
    }

    public Account getAccountById(int id) {
        return allAccountsById.getOrDefault(id, null);
    }

    public void addAccount(Account account) {
        allAccountsById.put(account.getId(), account);
    }
}
