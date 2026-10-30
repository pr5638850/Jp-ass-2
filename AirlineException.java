package airlinereservation;

/** Base class for all business-rule and validation errors in the system. */
public class AirlineException extends RuntimeException {
    public AirlineException(String message) {
        super(message);
    }
}
