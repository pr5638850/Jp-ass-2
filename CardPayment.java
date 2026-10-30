package airlinereservation;

public class CardPayment extends Payment {
    private final String lastFour;

    public CardPayment(String id, String bookingId, double amount, String cardNumber) {
        super(id, bookingId, amount);
        String digits = cardNumber == null ? "" : cardNumber.replace(" ", "");
        if (!digits.matches("[0-9]{16}")) {
            throw new PaymentException("Card number must contain exactly 16 digits.");
        }
        this.lastFour = digits.substring(12);
    }

    @Override
    public String getMethod() { return "Credit/Debit Card"; }

    @Override
    public String getMaskedDetails() { return "XXXX-XXXX-XXXX-" + lastFour; }
}
