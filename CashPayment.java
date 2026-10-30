package airlinereservation;

public class CashPayment extends Payment {
    public CashPayment(String id, String bookingId, double amount) {
        super(id, bookingId, amount);
    }

    @Override
    public String getMethod() { return "Cash"; }

    @Override
    public String getMaskedDetails() { return "Paid at airline counter"; }
}
