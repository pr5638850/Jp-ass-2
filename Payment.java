package airlinereservation;

import java.time.LocalDateTime;

/** Abstract payment. Each payment method supplies its own name and masked details (polymorphism). */
public abstract class Payment {
    private final String id;
    private final String bookingId;
    private final double amount;
    private final LocalDateTime paidAt;

    protected Payment(String id, String bookingId, double amount) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paidAt = LocalDateTime.now();
    }

    public abstract String getMethod();

    public abstract String getMaskedDetails();

    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public double getAmount() { return amount; }
    public LocalDateTime getPaidAt() { return paidAt; }

    @Override
    public String toString() {
        return id + " | " + getMethod() + " | " + getMaskedDetails()
                + String.format(" | Rs. %.2f | PAID at ", amount)
                + paidAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
