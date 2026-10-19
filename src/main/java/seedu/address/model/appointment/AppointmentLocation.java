package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Value object representing an appointment location.
 */
public final class AppointmentLocation {
    private final String value;

    /**
     * Constructs an {@code AppointmentLocation} with the specified value.
     *
     * @param value The location of the appointment.
     */
    public AppointmentLocation(String value) {
        requireNonNull(value);
        checkArgument(isValid(value), "Appointment location cannot be empty.");
        this.value = value.strip();
    }

    /** Returns whether the supplied value is a non-blank appointment location. */
    public static boolean isValid(String value) {
        requireNonNull(value);
        return !value.strip().isEmpty();
    }

    public String getValue() {
        return value;
    }

    @Override public String toString() {
        return value;
    }

    @Override public boolean equals(Object other) {
        return other instanceof AppointmentLocation location && value.equals(location.value);
    }

    @Override public int hashCode() {
        return value.hashCode();
    }
}
