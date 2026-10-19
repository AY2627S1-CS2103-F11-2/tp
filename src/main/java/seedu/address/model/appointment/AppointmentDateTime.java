package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

/**
 * Value object representing an appointment date-time.
 */
public final class AppointmentDateTime implements Comparable<AppointmentDateTime> {
    private final LocalDateTime value;

    public AppointmentDateTime(LocalDateTime value) {
        this.value = requireNonNull(value);
    }

    public LocalDateTime getValue() {
        return value;
    }

    @Override public int compareTo(AppointmentDateTime other) {
        return value.compareTo(other.value);
    }

    @Override public String toString() {
        return value.toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof AppointmentDateTime dateTime && value.equals(dateTime.value);
    }

    @Override public int hashCode() {
        return value.hashCode();
    }
}
