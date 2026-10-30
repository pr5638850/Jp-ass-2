package airlinereservation;

public class UpiPayment extends Payment {
    private final String upiId;

    public UpiPayment(String id, String bookingId, double amount, String upiId) {
        super(id, bookingId, amount);
        if (upiId == null || !upiId.matches("[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}")) {
            throw new PaymentException("UPI ID must look like name@bank.");
        }
        this.upiId = upiId;
    }

    @Override
    public String getMethod() { return "UPI"; }

    @Override
    public String getMaskedDetails() { return upiId; }
}
