package airlinereservation;

public class BusinessSeat extends Seat {
    public BusinessSeat(String number) {
        super(number);
    }

    @Override
    public String getSeatClass() { return "Business"; }

    @Override
    public double getFareMultiplier() { return 2.0; }
}
