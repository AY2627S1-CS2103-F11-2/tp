package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

/**
 * Value object representing optional appointment remarks.
 */
public final class AppointmentRemarks {
    private final String value;

    public AppointmentRemarks(String value) {
        this.value = requireNonNull(value);
    }

    /** Returns whether the supplied remarks value is valid. Empty remarks are allowed. */
    public static boolean isValid(String value) {
        return value != null;
    }

    public String getValue() {
        return value;
    }

    @Override public String toString() {
        return value;
    }

    @Override public boolean equals(Object other) {
        return other instanceof AppointmentRemarks remarks && value.equals(remarks.value);
    }

    @Override public int hashCode() {
        return value.hashCode();
    }
}
