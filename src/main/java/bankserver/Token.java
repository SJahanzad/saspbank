package bankserver;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Token {
    private static Map<String, Token> allTokens = new HashMap<>();
    private LocalDateTime expiryTime;
    private String value;
    private Account account;

    public Token(Account account) {
        this.account = account;
        value = generateTokenValue();
        expiryTime = LocalDateTime.now().plusHours(1);
        allTokens.put(value, this);
    }

    public static Account getAccount(String token) throws InvalidTokenException, ExpiredTokenException {
        Token resultToken = allTokens.get(token);
        if (resultToken == null)
            throw new InvalidTokenException();
        if (resultToken.hasExpired())
            throw new ExpiredTokenException();
        return resultToken.getAccount();
    }

    public Account getAccount() {
        return account;
    }

    private String generateTokenValue() {
        Random random = new Random();
        String chars = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 32; i++)
            token.append(chars.charAt(random.nextInt(chars.length())));
        return token.toString();
    }

    public boolean hasExpired() {
        return LocalDateTime.now().compareTo(expiryTime) > 0;
    }

    @Override
    public String toString() {
        return value;
    }
}
