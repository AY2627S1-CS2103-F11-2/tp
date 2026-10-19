
package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Value object representing an appointment date-time.
 * Guarantees: immutable; is valid as declared in {@link #isValid(LocalDateTime)}.
 */
public final class AppointmentDateTime implements Comparable<AppointmentDateTime> {

    public static final String MESSAGE_CONSTRAINTS =
            "Appointment date-time must use YYYYMMDD-HHMM format "
                    + "and represent a valid date and time.";

    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd-HHmm")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final LocalDateTime value;

    /**
     * Constructs an {@code AppointmentDateTime} with the specified value.
     *
     * @param value The DateTime (in LocalDateTime Object) of the appointment.
     */
    public AppointmentDateTime(LocalDateTime value) {
        this.value = requireNonNull(value);
    }

    /** Returns true if a given {@link LocalDateTime} is non-null. */
    public static boolean isValid(LocalDateTime test) {
        return test != null;
    }

    /**
     * Returns the appointment date-time.
     */
    public LocalDateTime getValue() {
        return value;
    }

    @Override
    public int compareTo(AppointmentDateTime other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value.format(INPUT_FORMAT);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AppointmentDateTime otherDateTime)) {
            return false;
        }

        return value.equals(otherDateTime.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
