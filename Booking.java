package airlinereservation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** A booking links one flight, one passenger and one or more seats (Business Rule 5). */
public class Booking {
    public static final double GST_RATE = 0.12;
    public static final double AIRPORT_FEE_PER_SEAT = 150.0;

    private final String id;
    private final Flight flight;
    private final Passenger passenger;
    private final List<Seat> seats;
    private final double fare;
    private final double gst;
    private final double airportFee;
    private final double total;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;
    private Payment payment;
    private double refundAmount;
    private String refundNote = "";

    public Booking(String id, Flight flight, Passenger passenger, List<Seat> seats) {
        this.id = id;
        this.flight = flight;
        this.passenger = passenger;
        this.seats = new ArrayList<>(seats);
        double sum = 0;
        for (Seat seat : seats) {
            sum += seat.calculateFare(flight.getBaseFare());
        }
        this.fare = round(sum);
        this.gst = round(fare * GST_RATE);
        this.airportFee = round(AIRPORT_FEE_PER_SEAT * seats.size());
        this.total = round(fare + gst + airportFee);
    }

    public void confirmPayment(Payment payment) {
        if (status != BookingStatus.PENDING_PAYMENT) {
            throw new PaymentException("Payment is not required. Booking status: " + status);
        }
        if (Math.abs(payment.getAmount() - total) > 0.001) {
            throw new PaymentException("Payment amount must equal the total amount of Rs. "
                    + String.format("%.2f", total));
        }
        this.payment = payment;
        this.status = BookingStatus.CONFIRMED;
    }

    public void markCheckedIn() {
        if (status != BookingStatus.CONFIRMED) {
            throw new BookingException("Only a paid (CONFIRMED) booking can be checked in.");
        }
        status = BookingStatus.CHECKED_IN;
    }

    /** Cancels the booking, releases every seat immediately and applies the refund policy. */
    public double cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new BookingException("Booking " + id + " is already cancelled.");
        }
        if (status == BookingStatus.CHECKED_IN) {
            throw new BookingException("Checked-in bookings cannot be cancelled.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!flight.getDeparture().isAfter(now)) {
            throw new BookingException("Flight has already departed; booking cannot be cancelled.");
        }
        long hours = Duration.between(now, flight.getDeparture()).toHours();
        double refund;
        if (status == BookingStatus.PENDING_PAYMENT) {
            refund = 0;
            refundNote = "No payment was made, so there is nothing to refund.";
        } else if (hours >= 24) {
            refund = total * 0.90;
            refundNote = "Cancelled 24+ hours before departure: 90% refund.";
        } else if (hours >= 2) {
            refund = total * 0.50;
            refundNote = "Cancelled 2-24 hours before departure: 50% refund.";
        } else {
            refund = 0;
            refundNote = "Cancelled less than 2 hours before departure: no refund.";
        }
        for (Seat seat : seats) {
            seat.release();
        }
        refundAmount = round(refund);
        status = BookingStatus.CANCELLED;
        return refundAmount;
    }

    public boolean isActive() {
        return status != BookingStatus.CANCELLED;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getSeatNumbers() {
        StringBuilder sb = new StringBuilder();
        for (Seat seat : seats) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(seat.getNumber());
        }
        return sb.toString();
    }

    public String getReceipt() {
        String bar = "=".repeat(58);
        StringBuilder sb = new StringBuilder();
        sb.append(bar).append("\n");
        sb.append("                    BOOKING RECEIPT\n");
        sb.append(bar).append("\n");
        sb.append(String.format("%-13s: %s\n", "Booking ID", id));
        sb.append(String.format("%-13s: %s\n", "Status", status));
        sb.append(String.format("%-13s: %s (%s)\n", "Passenger", passenger.getName(),
                passenger.getId()));
        sb.append(String.format("%-13s: %s\n", "Flight", flight.getFlightNumber()
                + " | " + flight.getOrigin() + " -> " + flight.getDestination()));
        sb.append(String.format("%-13s: %s | Gate %s\n", "Departure",
                flight.getDeparture().format(Flight.FORMAT), flight.getGate()));
        sb.append(String.format("%-13s: %s\n", "Booked on",
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        sb.append("-".repeat(58)).append("\n");
        for (Seat seat : seats) {
            sb.append(String.format("Seat %-4s %-9s (x%.1f)            Rs. %10.2f\n",
                    seat.getNumber(), seat.getSeatClass(), seat.getFareMultiplier(),
                    seat.calculateFare(flight.getBaseFare())));
        }
        sb.append("-".repeat(58)).append("\n");
        sb.append(String.format("%-38s Rs. %10.2f\n", "Fare", fare));
        sb.append(String.format("%-38s Rs. %10.2f\n", "GST (12%)", gst));
        sb.append(String.format("%-38s Rs. %10.2f\n", "Airport fee (Rs. 150 per seat)", airportFee));
        sb.append(String.format("%-38s Rs. %10.2f\n", "Taxes & fees total", gst + airportFee));
        sb.append("-".repeat(58)).append("\n");
        sb.append(String.format("%-38s Rs. %10.2f\n", "TOTAL AMOUNT", total));
        sb.append(bar).append("\n");
        sb.append("Payment      : ").append(payment == null ? "PENDING"
                : payment.toString()).append("\n");
        sb.append(bar);
        return sb.toString();
    }

    public String getCancellationReceipt() {
        String bar = "=".repeat(58);
        return bar + "\n"
                + "                 CANCELLATION RECEIPT\n"
                + bar + "\n"
                + String.format("%-13s: %s\n", "Booking ID", id)
                + String.format("%-13s: %s\n", "Passenger", passenger.getName())
                + String.format("%-13s: %s\n", "Flight", flight.getFlightNumber())
                + String.format("%-13s: %s (released)\n", "Seats", getSeatNumbers())
                + String.format("%-13s: Rs. %.2f\n", "Amount paid", payment == null ? 0.0 : total)
                + String.format("%-13s: Rs. %.2f\n", "Refund", refundAmount)
                + String.format("%-13s: %s\n", "Policy", refundNote)
                + String.format("%-13s: %s\n", "Status", status)
                + bar;
    }

    public String getId() { return id; }
    public Flight getFlight() { return flight; }
    public Passenger getPassenger() { return passenger; }
    public List<Seat> getSeats() { return seats; }
    public double getFare() { return fare; }
    public double getTotal() { return total; }
    public BookingStatus getStatus() { return status; }
    public Payment getPayment() { return payment; }
    public double getRefundAmount() { return refundAmount; }

    @Override
    public String toString() {
        return id + " | " + passenger.getName() + " | " + flight.getFlightNumber()
                + " | Seats: " + getSeatNumbers() + String.format(" | Rs. %.2f | ", total) + status;
    }
}
