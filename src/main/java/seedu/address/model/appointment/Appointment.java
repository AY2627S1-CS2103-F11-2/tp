package seedu.address.model.appointment;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

import seedu.address.model.person.Person;

/** An immutable appointment. All date-times are Singapore local date-times. */
public final class Appointment {
    public static final String MESSAGE_INVALID_NAME =
            "Appointment name must contain only letters and spaces, with 1-80 characters.";
    public static final String MESSAGE_INVALID_LOCATION = "Appointment location cannot be empty.";
    public static final String MESSAGE_INVALID_PERIOD = "Appointment end must be after its start.";
    public static final Comparator<Appointment> CHRONOLOGICAL_ORDER = Comparator
            .comparing(Appointment::getStart).thenComparing(Appointment::getEnd)
            .thenComparing(Appointment::getName).thenComparing(a -> a.getClient().getName().fullName);

    private final String name;
    private final Person client;
    private final String location;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String remarks;

    /** Creates an appointment after validating its fields and period. */
    public Appointment(String name, Person client, String location, LocalDateTime start,
            LocalDateTime end, String remarks) {
        requireAllNonNull(name, client, location, start, end, remarks);
        checkArgument(isValidName(name.trim()), MESSAGE_INVALID_NAME);
        checkArgument(isValidLocation(location), MESSAGE_INVALID_LOCATION);
        checkArgument(end.isAfter(start), MESSAGE_INVALID_PERIOD);
        this.name = name.trim();
        this.client = client;
        this.location = location.strip();
        this.start = start;
        this.end = end;
        this.remarks = remarks;
    }

    public static boolean isValidName(String name) {
        return name.length() <= 80 && name.matches("[A-Za-z ]*") && name.matches(".*[A-Za-z].*");
    }

    public static boolean isValidLocation(String location) {
        return !location.strip().isEmpty();
    }

    public String getName() {
        return name;
    }

    public Person getClient() {
        return client;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getRemarks() {
        return remarks;
    }

    /** Returns a copy linked to the updated client. */
    public Appointment withClient(Person updatedClient) {
        return new Appointment(name, updatedClient, location, start, end, remarks);
    }

    /** Adjacent appointments do not overlap. */
    public boolean overlaps(Appointment other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Appointment appointment)) {
            return false;
        }
        return name.equals(appointment.name) && client.equals(appointment.client)
                && location.equals(appointment.location) && start.equals(appointment.start)
                && end.equals(appointment.end) && remarks.equals(appointment.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, client, location, start, end, remarks);
    }
}
