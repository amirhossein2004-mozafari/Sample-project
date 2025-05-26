import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class User {
    private String username;
    private String hashedPassword;
    private String accountNumber;
    private List<Account> accounts;

    public User(String username, String password) {
        this.username = username;
        this.hashedPassword = hashPassword(password);
        this.accountNumber = generateUniqueAccountNumber();
        this.accounts = new ArrayList<>();
        this.accounts.add(new Account());
    }

    private String generateUniqueAccountNumber() {
        String accNumber;
        do {
            accNumber = String.format("%08d", ThreadLocalRandom.current().nextInt(100000000));
        } while (BankingSystem.accountNumbers.contains(accNumber));
        BankingSystem.accountNumbers.add(accNumber);
        return accNumber;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("خطا در الگوریتم هشینگ");
        }
    }

    public boolean validatePassword(String password) {
        return this.hashedPassword.equals(hashPassword(password));
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getHashedPassword() {
        return hashedPassword;
    }


    public String getAccountNumber() { return accountNumber; }
    public String getUsername() { return username; }
    public List<Account> getAccounts() { return accounts; }
}