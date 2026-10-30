package airlinereservation;

/** Thrown when a booking operation violates a business rule (check-in, cancel, etc.). */
public class BookingException extends AirlineException {
    public BookingException(String message) {
        super(message);
    }
}
