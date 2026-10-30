package airlinereservation;

public class Passenger {
    private final String id;
    private final String name;
    private final int age;
    private final String phone;

    public Passenger(String id, String name, int age, String phone) {
        if (name == null || !name.matches("[A-Za-z][A-Za-z .]{1,39}")) {
            throw new InvalidInputException("Name must contain only letters/spaces (2-40 characters).");
        }
        if (age < 1 || age > 120) {
            throw new InvalidInputException("Age must be between 1 and 120.");
        }
        if (phone == null || !phone.matches("[6-9][0-9]{9}")) {
            throw new InvalidInputException("Phone must be a valid 10-digit mobile number.");
        }
        this.id = id;
        this.name = name.trim();
        this.age = age;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return id + " | " + name + " | Age: " + age + " | Phone: " + phone;
    }
}
