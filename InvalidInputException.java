package airlinereservation;

/** Thrown when user supplied data fails validation. */
public class InvalidInputException extends AirlineException {
    public InvalidInputException(String message) {
        super(message);
    }
}
