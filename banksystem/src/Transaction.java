import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class Transaction {
    public enum Type { DEPOSIT, WITHDRAW, TRANSFER }

    private static final AtomicLong idCounter = new AtomicLong(1);

    private final long id;
    private final Type type;
    private final double amount;
    private final String source;
    private final String destination;
    private LocalDateTime timestamp;

    public Transaction(Type type, double amount, String source, String destination) {
        this.id = idCounter.getAndIncrement();
        this.type = type;
        this.amount = amount;
        this.source = source;
        this.destination = destination;
        this.timestamp = LocalDateTime.now();
    }


    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    public long getId() { return id; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}