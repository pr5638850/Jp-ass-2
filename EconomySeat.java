package airlinereservation;

public class EconomySeat extends Seat {
    public EconomySeat(String number) {
        super(number);
    }

    @Override
    public String getSeatClass() { return "Economy"; }

    @Override
    public double getFareMultiplier() { return 1.0; }
}
