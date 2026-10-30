package airlinereservation;

/** Thrown when a payment is invalid or not allowed for the booking. */
public class PaymentException extends AirlineException {
    public PaymentException(String message) {
        super(message);
    }
}
