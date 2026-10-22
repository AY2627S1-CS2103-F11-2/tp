package seedu.address.model.appointment;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

import seedu.address.model.person.Person;

/** An immutable appointment. All date-times are Singapore local date-times. */
public final class Appointment {
    public static final String MESSAGE_INVALID_LOCATION = "Appointment location cannot be empty.";
    public static final String MESSAGE_INVALID_PERIOD = "Appointment end must be after its start.";
    public static final Comparator<Appointment> CHRONOLOGICAL_ORDER = Comparator
            .comparing(Appointment::getStart).thenComparing(Appointment::getEnd)
            .thenComparing(Appointment::getName).thenComparing(a -> a.getClient().getName().fullName);

    private final AppointmentName name;
    private final Person client;
    private final AppointmentLocation location;
    private final AppointmentDateTime start;
    private final AppointmentDateTime end;
    private final AppointmentRemarks remarks;

    /** Creates an appointment after validating its fields and period. */
    public Appointment(String name, Person client, String location, LocalDateTime start,
            LocalDateTime end, String remarks) {
        requireAllNonNull(name, client, location, start, end, remarks);
        this.name = new AppointmentName(name);
        this.client = client;
        this.location = new AppointmentLocation(location);
        this.start = new AppointmentDateTime(start);
        this.end = new AppointmentDateTime(end);
        checkArgument(this.end.compareTo(this.start) > 0, MESSAGE_INVALID_PERIOD);
        this.remarks = new AppointmentRemarks(remarks);
    }

    public String getName() {
        return name.getValue();
    }

    public Person getClient() {
        return client;
    }

    public String getLocation() {
        return location.getValue();
    }

    public LocalDateTime getStart() {
        return start.getValue();
    }

    public LocalDateTime getEnd() {
        return end.getValue();
    }

    public String getRemarks() {
        return remarks.getValue();
    }

    public static boolean isValidName(String apptName) {
        return AppointmentName.isValidForCreation(apptName);
    }

    public static boolean isValidLocation(String loc) {
        return AppointmentLocation.isValid(loc);
    }

    /** Returns a copy linked to the updated client. */
    public Appointment withClient(Person updatedClient) {
        return new Appointment(name.getValue(), updatedClient, location.getValue(), start.getValue(),
                end.getValue(), remarks.getValue());
    }

    /** Adjacent appointments do not overlap. */
    public boolean overlaps(Appointment other) {
        requireAllNonNull(other);
        return start.compareTo(other.end) < 0 && end.compareTo(other.start) > 0;
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
