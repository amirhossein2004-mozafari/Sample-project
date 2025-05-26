import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private double balance;
    private List<Transaction> transactions;

    public Account() {
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public void deposit(double amount, boolean logTransaction) {
        balance += amount;
        if(logTransaction) {
            transactions.add(new Transaction(
                    Transaction.Type.DEPOSIT,
                    amount,
                    "System",
                    "Account"
            ));
        }
    }

    public void withdraw(double amount, boolean logTransaction) throws InsufficientFundsException {
        if(balance < amount) throw new InsufficientFundsException();
        balance -= amount;
        if(logTransaction) {
            transactions.add(new Transaction(
                    Transaction.Type.WITHDRAW,
                    amount,
                    "Account",
                    "System"
            ));
        }
    }



    public void forceWithdraw(double amount) {
        balance -= amount;
        transactions.add(new Transaction(
                Transaction.Type.WITHDRAW,
                amount,
                "Account",
                "System (Undo)"
        ));
    }

    public Transaction findTransactionById(long id) {
        for(Transaction t : transactions) {
            if(t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public boolean removeTransaction(Transaction transaction) {
        return transactions.remove(transaction);
    }


    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}