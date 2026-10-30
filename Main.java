package airlinereservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final AirlineService service = new AirlineService();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   AIRLINE RESERVATION & CHECK-IN SYSTEM");
        System.out.println("==============================================");
        int choice = 0;
        do {
            printMenu();
            try {
                choice = readInt("Enter your choice: ", 1, 8);
                switch (choice) {
                    case 1: addFlight(); break;
                    case 2: addPassenger(); break;
                    case 3: createBooking(); break;
                    case 4: makePayment(); break;
                    case 5: checkIn(); break;
                    case 6: cancelBooking(); break;
                    case 7: displayFlights(); break;
                    case 8:
                        System.out.println("Thank you for using the Airline Reservation System. Goodbye!");
                        break;
                    default: break;
                }
            } catch (AirlineException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (NoSuchElementException ex) {
                System.out.println("Input ended. Exiting.");
                choice = 8;
            } catch (Exception ex) {
                System.out.println("Unexpected error: " + ex.getMessage());
            }
            System.out.println();
        } while (choice != 8);
        sc.close();
    }

    private static void printMenu() {
        System.out.println("--------------- MAIN MENU ----------------");
        System.out.println("1. Add Flight");
        System.out.println("2. Add Passenger");
        System.out.println("3. Create Booking");
        System.out.println("4. Make Payment");
        System.out.println("5. Check-in & Issue Boarding Pass");
        System.out.println("6. Cancel Booking");
        System.out.println("7. Display Flights");
        System.out.println("8. Exit");
        System.out.println("------------------------------------------");
    }

    private static void addFlight() {
        String number = readText("Enter flight number (e.g. AI101): ").toUpperCase();
        String origin = readText("Enter origin city: ");
        String destination = readText("Enter destination city: ");
        LocalDateTime departure = readDateTime("Enter departure (yyyy-MM-dd HH:mm): ");
        double fare = readDouble("Enter base fare (Rs.): ");
        String gate = readText("Enter boarding gate (e.g. G12): ");
        String model = readText("Enter aircraft model: ");
        int rows = readInt("Enter number of seat rows (1-40, 6 seats per row): ", 1, 40);
        int business = readInt("Enter number of business rows (0-" + rows + "): ", 0, rows);
        Flight flight = service.addFlight(number, origin, destination, departure, fare, gate, model,
                rows, business);
        System.out.println("Flight added successfully: " + flight);
        System.out.println(flight.getAvailabilitySummary());
    }

    private static void addPassenger() {
        String name = readText("Enter passenger name: ");
        int age = readInt("Enter age: ", 1, 120);
        String phone = readText("Enter 10-digit phone number: ");
        Passenger passenger = service.addPassenger(name, age, phone);
        System.out.println("Passenger registered successfully. Passenger ID: " + passenger.getId());
    }

    private static void createBooking() {
        String flightNumber = readText("Enter flight number: ");
        Flight flight = service.getFlight(flightNumber);
        System.out.println(flight.getAvailabilitySummary());
        System.out.print(flight.getSeatMap());
        String passengerId = readText("Enter passenger ID: ");
        String seats = readText("Enter seat(s) separated by commas (e.g. 1A,3B): ");
        Booking booking = service.createBooking(flightNumber, passengerId, seats);
        System.out.println("Booking created. Seats are held until payment is confirmed.");
        System.out.println(booking.getReceipt());
        System.out.println("Updated " + flight.getAvailabilitySummary());
    }

    private static void makePayment() {
        String bookingId = readText("Enter booking ID: ");
        Booking booking = service.getBooking(bookingId);
        System.out.printf("Amount payable: Rs. %.2f%n", booking.getTotal());
        System.out.println("1. Credit/Debit Card   2. UPI   3. Cash");
        int method = readInt("Select payment method: ", 1, 3);
        String detail = "";
        if (method == 1) detail = readText("Enter 16-digit card number: ");
        if (method == 2) detail = readText("Enter UPI ID (name@bank): ");
        String confirm = readText("Confirm payment of Rs. " + String.format("%.2f", booking.getTotal())
                + "? (Y/N): ");
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Payment cancelled. Booking remains PENDING_PAYMENT.");
            return;
        }
        service.makePayment(bookingId, method, detail);
        System.out.println("Payment successful. Booking confirmed.");
        System.out.println(booking.getReceipt());
    }

    private static void checkIn() {
        String bookingId = readText("Enter booking ID: ");
        List<BoardingPass> passes = service.checkIn(bookingId);
        System.out.println("Check-in successful. Seats assigned.");
        for (BoardingPass pass : passes) {
            System.out.println(pass.display());
        }
        System.out.println("Updated "
                + passes.get(0).getBooking().getFlight().getAvailabilitySummary());
    }

    private static void cancelBooking() {
        String bookingId = readText("Enter booking ID to cancel: ");
        Booking booking = service.cancelBooking(bookingId);
        System.out.println(booking.getCancellationReceipt());
        System.out.println("Updated " + booking.getFlight().getAvailabilitySummary());
    }

    private static void displayFlights() {
        System.out.println("================= FLIGHT DETAILS =================");
        if (service.getFlights().isEmpty()) {
            System.out.println("No flights available.");
            return;
        }
        for (Flight flight : service.getFlights()) {
            System.out.println(flight);
            System.out.println(flight.getAvailabilitySummary());
            System.out.println("Seat map (XX = booked, CI = checked-in, * = business row):");
            System.out.print(flight.getSeatMap());
            System.out.println("--------------------------------------------------");
        }
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                    System.out.println("Enter a positive numeric value.");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid numeric value.");
            }
        }
    }

    private static LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return LocalDateTime.parse(input, Flight.FORMAT);
            } catch (DateTimeParseException ex) {
                System.out.println("Invalid format. Use yyyy-MM-dd HH:mm (e.g. 2026-11-15 09:30).");
            }
        }
    }
}
