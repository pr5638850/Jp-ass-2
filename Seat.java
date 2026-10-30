package airlinereservation;

/** Abstract seat. Subclasses decide the seat class and its fare multiplier (polymorphism). */
public abstract class Seat {
    private final String number;
    private SeatStatus status = SeatStatus.AVAILABLE;

    protected Seat(String number) {
        this.number = number;
    }

    public abstract String getSeatClass();

    public abstract double getFareMultiplier();

    public double calculateFare(double baseFare) {
        return baseFare * getFareMultiplier();
    }

    public String getNumber() { return number; }
    public SeatStatus getStatus() { return status; }
    public boolean isAvailable() { return status == SeatStatus.AVAILABLE; }

    public void reserve() {
        if (status != SeatStatus.AVAILABLE) {
            throw new SeatUnavailableException("Seat " + number + " is not available (status: " + status
                    + ").");
        }
        status = SeatStatus.BOOKED;
    }

    public void release() {
        status = SeatStatus.AVAILABLE;
    }

    public void checkIn() {
        if (status != SeatStatus.BOOKED) {
            throw new BookingException("Seat " + number + " must be booked before check-in.");
        }
        status = SeatStatus.CHECKED_IN;
    }

    @Override
    public String toString() {
        return number + " (" + getSeatClass() + ", " + status + ")";
    }
}
