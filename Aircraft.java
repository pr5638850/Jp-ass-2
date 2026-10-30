package airlinereservation;

import java.util.ArrayList;
import java.util.List;

/** Aircraft describes the seat layout (rows x 6 columns). Business rows come first. */
public class Aircraft {
    public static final String COLUMNS = "ABCDEF";

    private final String id;
    private final String model;
    private final int rows;
    private final int businessRows;

    public Aircraft(String id, String model, int rows, int businessRows) {
        if (rows < 1 || rows > 40) {
            throw new InvalidInputException("Rows must be between 1 and 40.");
        }
        if (businessRows < 0 || businessRows > rows) {
            throw new InvalidInputException("Business rows must be between 0 and " + rows + ".");
        }
        this.id = id;
        this.model = model;
        this.rows = rows;
        this.businessRows = businessRows;
    }

    /** Builds a fresh set of seats for one flight (each flight tracks its own seat status). */
    public List<Seat> createSeatLayout() {
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= rows; row++) {
            for (int col = 0; col < COLUMNS.length(); col++) {
                String number = row + "" + COLUMNS.charAt(col);
                seats.add(row <= businessRows ? new BusinessSeat(number) : new EconomySeat(number));
            }
        }
        return seats;
    }

    public String getId() { return id; }
    public String getModel() { return model; }
    public int getRows() { return rows; }
    public int getBusinessRows() { return businessRows; }
    public int getCapacity() { return rows * COLUMNS.length(); }

    @Override
    public String toString() {
        return id + " " + model + " (" + getCapacity() + " seats)";
    }
}
