package airlinereservation;

/** Thrown when a seat cannot be booked because it is not available (prevents double booking). */
public class SeatUnavailableException extends AirlineException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
