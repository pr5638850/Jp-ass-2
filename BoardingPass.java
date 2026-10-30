package airlinereservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One boarding pass is issued per checked-in seat. */
public class BoardingPass {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String number;
    private final Booking booking;
    private final Seat seat;
    private final LocalDateTime boardingTime;
    private final LocalDateTime issuedAt;

    public BoardingPass(String number, Booking booking, Seat seat) {
        this.number = number;
        this.booking = booking;
        this.seat = seat;
        this.boardingTime = booking.getFlight().getDeparture().minusMinutes(40);
        this.issuedAt = LocalDateTime.now();
    }

    private static String line(String text) {
        return String.format("| %-52s |", text);
    }

    public String display() {
        Flight flight = booking.getFlight();
        String border = "+" + "-".repeat(54) + "+";
        StringBuilder sb = new StringBuilder();
        sb.append(border).append("\n");
        sb.append(line("            *** BOARDING PASS ***")).append("\n");
        sb.append(border).append("\n");
        sb.append(line("Pass No   : " + number)).append("\n");
        sb.append(line("Passenger : " + booking.getPassenger().getName())).append("\n");
        sb.append(line("Booking   : " + booking.getId())).append("\n");
        sb.append(line("Flight    : " + flight.getFlightNumber() + " | "
                + flight.getOrigin() + " -> " + flight.getDestination())).append("\n");
        sb.append(line("Seat      : " + seat.getNumber() + " (" + seat.getSeatClass()
                + ")")).append("\n");
        sb.append(line("Gate      : " + flight.getGate())).append("\n");
        sb.append(line("Boarding  : " + boardingTime.format(Flight.FORMAT))).append("\n");
        sb.append(line("Departure : " + flight.getDeparture().format(Flight.FORMAT))).append("\n");
        sb.append(line("Issued at : " + issuedAt.format(STAMP))).append("\n");
        sb.append(border);
        return sb.toString();
    }

    public String getNumber() { return number; }
    public Booking getBooking() { return booking; }
    public Seat getSeat() { return seat; }
    public LocalDateTime getBoardingTime() { return boardingTime; }
    public LocalDateTime getIssuedAt() { return issuedAt; }

    @Override
    public String toString() {
        return number + " | " + booking.getPassenger().getName() + " | Seat " + seat.getNumber();
    }
}
