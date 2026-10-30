package airlinereservation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Holds all data and enforces every business rule. Main only handles input/output. */
public class AirlineService {
    private final List<Flight> flights = new ArrayList<>();
    private final List<Passenger> passengers = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<BoardingPass> boardingPasses = new ArrayList<>();

    private int aircraftCounter = 1;
    private int passengerCounter = 1;
    private int bookingCounter = 1;
    private int paymentCounter = 1;
    private int passCounter = 1;

    public Flight addFlight(String flightNumber, String origin, String destination,
                            LocalDateTime departure, double baseFare, String gate,
                            String aircraftModel, int rows, int businessRows) {
        flightNumber = flightNumber.toUpperCase();
        if (findFlight(flightNumber) != null) {
            throw new AirlineException("Flight " + flightNumber + " already exists.");
        }
        if (!departure.isAfter(LocalDateTime.now())) {
            throw new InvalidInputException("Departure must be a future date and time.");
        }
        Aircraft aircraft = new Aircraft(String.format("AC%03d", aircraftCounter), aircraftModel, rows,
                businessRows);
        Flight flight = new Flight(flightNumber, origin, destination, departure, baseFare,
                gate.toUpperCase(), aircraft);
        aircraftCounter++;
        flights.add(flight);
        return flight;
    }

    public Passenger addPassenger(String name, int age, String phone) {
        Passenger passenger = new Passenger(String.format("P%03d", passengerCounter), name, age, phone);
        passengerCounter++;
        passengers.add(passenger);
        return passenger;
    }

    /**
     * Rule 1: seats can be booked only if available. Rule 5: booking links flight, passenger and
     * seats.
     */
    public Booking createBooking(String flightNumber, String passengerId, String seatInput) {
        Flight flight = getFlight(flightNumber);
        Passenger passenger = getPassenger(passengerId);
        if (flight.hasDeparted()) {
            throw new BookingException("Flight " + flight.getFlightNumber() + " has already departed.");
        }
        for (Booking existing : bookings) {
            if (existing.isActive() && existing.getFlight() == flight
                    && existing.getPassenger() == passenger) {
                throw new BookingException("Passenger " + passenger.getId()
                        + " already has an active booking ("
                        + existing.getId() + ") on this flight.");
            }
        }

        // validate every seat first, then reserve, so a failed request never leaves seats half-booked
        List<Seat> chosen = new ArrayList<>();
        for (String number : parseSeatNumbers(seatInput)) {
            Seat seat = flight.findSeat(number);
            if (seat == null) {
                throw new InvalidInputException("Seat " + number + " does not exist on flight "
                        + flight.getFlightNumber() + ".");
            }
            if (!seat.isAvailable()) {
                throw new SeatUnavailableException("Seat " + number + " is already " + seat.getStatus()
                        + ". Double booking is not allowed.");
            }
            chosen.add(seat);
        }
        for (Seat seat : chosen) {
            seat.reserve();
        }
        Booking booking = new Booking(String.format("B%03d", bookingCounter++), flight, passenger,
                chosen);
        bookings.add(booking);
        return booking;
    }

    private Set<String> parseSeatNumbers(String input) {
        Set<String> numbers = new LinkedHashSet<>();
        for (String part : input.split(",")) {
            String seat = part.trim().toUpperCase();
            if (!seat.matches("[0-9]{1,2}[A-F]")) {
                throw new InvalidInputException("Invalid seat '" + part.trim()
                        + "'. Use a format like 1A, 12F.");
            }
            numbers.add(seat);
        }
        if (numbers.size() > 6) {
            throw new InvalidInputException("A maximum of 6 seats can be booked at a time.");
        }
        return numbers;
    }

    /** Rule 2 (part 1): payment confirmation. method 1 = Card, 2 = UPI, 3 = Cash. */
    public Payment makePayment(String bookingId, int method, String detail) {
        Booking booking = getBooking(bookingId);
        if (booking.getFlight().hasDeparted()) {
            throw new PaymentException("Flight has already departed; payment not allowed.");
        }
        String paymentId = String.format("PAY%03d", paymentCounter);
        Payment payment;
        switch (method) {
            case 1:  payment = new CardPayment(paymentId, booking.getId(), booking.getTotal(),
                    detail); break;
            case 2:  payment = new UpiPayment(paymentId, booking.getId(), booking.getTotal(),
                    detail); break;
            case 3:  payment = new CashPayment(paymentId, booking.getId(), booking.getTotal()); break;
            default: throw new PaymentException("Unknown payment method.");
        }
        booking.confirmPayment(payment);
        paymentCounter++;
        return payment;
    }

    /** Rules 2 and 4: boarding pass only after payment; check-in assigns the seats. */
    public List<BoardingPass> checkIn(String bookingId) {
        Booking booking = getBooking(bookingId);
        switch (booking.getStatus()) {
            case PENDING_PAYMENT:
                throw new BookingException("Check-in denied: payment is not confirmed for "
                        + booking.getId()
                        + ". Make the payment first.");
            case CANCELLED:
                throw new BookingException("Booking " + booking.getId() + " is cancelled.");
            case CHECKED_IN:
                throw new BookingException("Booking " + booking.getId() + " is already checked in.");
            default:
                break;
        }
        if (booking.getFlight().hasDeparted()) {
            throw new BookingException("Flight has already departed; check-in is closed.");
        }
        List<BoardingPass> passes = new ArrayList<>();
        for (Seat seat : booking.getSeats()) {
            seat.checkIn();
            BoardingPass pass = new BoardingPass(String.format("BP%03d", passCounter++), booking, seat);
            boardingPasses.add(pass);
            passes.add(pass);
        }
        booking.markCheckedIn();
        return passes;
    }

    /** Rule 3: cancellation releases seats immediately and applies the refund policy. */
    public Booking cancelBooking(String bookingId) {
        Booking booking = getBooking(bookingId);
        booking.cancel();
        return booking;
    }

    public Flight findFlight(String number) {
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equalsIgnoreCase(number)) return flight;
        }
        return null;
    }

    public Flight getFlight(String number) {
        Flight flight = findFlight(number);
        if (flight == null) throw new AirlineException("Flight " + number + " not found.");
        return flight;
    }

    public Passenger getPassenger(String id) {
        for (Passenger passenger : passengers) {
            if (passenger.getId().equalsIgnoreCase(id)) return passenger;
        }
        throw new AirlineException("Passenger " + id + " not found.");
    }

    public Booking getBooking(String id) {
        for (Booking booking : bookings) {
            if (booking.getId().equalsIgnoreCase(id)) return booking;
        }
        throw new AirlineException("Booking " + id + " not found.");
    }

    public List<Flight> getFlights() { return flights; }
    public List<Passenger> getPassengers() { return passengers; }
    public List<Booking> getBookings() { return bookings; }
    public List<BoardingPass> getBoardingPasses() { return boardingPasses; }
}
