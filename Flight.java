package airlinereservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Flight {
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departure;
    private final double baseFare;
    private final String gate;
    private final Aircraft aircraft;          // aggregation: a flight uses an aircraft
    private final List<Seat> seats;           // per-flight seat inventory

    public Flight(String flightNumber, String origin, String destination, LocalDateTime departure,
                  double baseFare, String gate, Aircraft aircraft) {
        if (flightNumber == null || !flightNumber.matches("[A-Z]{2}[0-9]{3,4}")) {
            throw new InvalidInputException(
                    "Flight number must look like AI101 (2 letters + 3-4 digits).");
        }
        if (origin.equalsIgnoreCase(destination)) {
            throw new InvalidInputException("Origin and destination cannot be the same.");
        }
        if (baseFare <= 0) {
            throw new InvalidInputException("Base fare must be greater than zero.");
        }
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departure = departure;
        this.baseFare = baseFare;
        this.gate = gate;
        this.aircraft = aircraft;
        this.seats = aircraft.createSeatLayout();
    }

    public Seat findSeat(String number) {
        for (Seat seat : seats) {
            if (seat.getNumber().equals(number)) return seat;
        }
        return null;
    }

    public int countSeats(SeatStatus status) {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.getStatus() == status) count++;
        }
        return count;
    }

    public boolean hasDeparted() {
        return !departure.isAfter(LocalDateTime.now());
    }

    public String getAvailabilitySummary() {
        return String.format("Seats -> Total: %d | Available: %d | Booked: %d | Checked-in: %d",
                seats.size(), countSeats(SeatStatus.AVAILABLE),
                countSeats(SeatStatus.BOOKED), countSeats(SeatStatus.CHECKED_IN));
    }

    /**
     * Draws the seat map. Free seats show their number, XX = booked, CI = checked-in, * = business
     * row.
     */
    public String getSeatMap() {
        StringBuilder sb = new StringBuilder();
        sb.append("        ");
        for (int c = 0; c < Aircraft.COLUMNS.length(); c++) {
            if (c == 3) sb.append("    ");
            sb.append(String.format("%-4s", Aircraft.COLUMNS.charAt(c)));
        }
        sb.append("\n");
        for (int row = 1; row <= aircraft.getRows(); row++) {
            sb.append(String.format("Row %2d%s ", row, row <= aircraft.getBusinessRows() ? "*" : " "));
            for (int c = 0; c < Aircraft.COLUMNS.length(); c++) {
                if (c == 3) sb.append("    ");
                Seat seat = seats.get((row - 1) * Aircraft.COLUMNS.length() + c);
                String symbol;
                switch (seat.getStatus()) {
                    case BOOKED:     symbol = "XX"; break;
                    case CHECKED_IN: symbol = "CI"; break;
                    default:         symbol = seat.getNumber();
                }
                sb.append(String.format("%-4s", symbol));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDeparture() { return departure; }
    public double getBaseFare() { return baseFare; }
    public String getGate() { return gate; }
    public Aircraft getAircraft() { return aircraft; }
    public List<Seat> getSeats() { return seats; }

    @Override
    public String toString() {
        return flightNumber + " | " + origin + " -> " + destination
                + " | Departs: " + departure.format(FORMAT)
                + " | Gate: " + gate
                + " | Aircraft: " + aircraft.getModel()
                + String.format(" | Base fare: Rs. %.2f", baseFare);
    }
}
