package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Value object representing an appointment name.
 */
public final class AppointmentName {
    public static final String MESSAGE_CREATION_CONSTRAINTS =
            "Appointment name must contain only letters and spaces, with 1-80 characters.";
    public static final String MESSAGE_CONSTRAINTS = "Appointment name must contain letters and spaces "
            + "(1-80 characters), or letters, digits and spaces (1-50 characters).";
    private final String value;

    /**
     * Constructs an {@code AppointmentName} with the specified value.
     *
     * @param value The name of the appointment.
     */
    public AppointmentName(String value) {
        requireNonNull(value);
        String normalized = value.trim();
        checkArgument(isValid(normalized), MESSAGE_CONSTRAINTS);
        this.value = normalized;
    }

    /** Returns whether the supplied value satisfies the appointment-name constraints. */
    public static boolean isValid(String value) {
        return isValidForCreation(value) || isValidForEditing(value);
    }

    /** Preserves the existing creation rules and compatibility with names stored before editing was extended. */
    public static boolean isValidForCreation(String value) {
        return value != null && value.length() <= 80
                && value.matches("[A-Za-z ]*") && value.matches(".*[A-Za-z].*");
    }

    /** Returns whether a replacement name satisfies the feature specification's edit-specific rules. */
    public static boolean isValidForEditing(String value) {
        return value != null && !value.isBlank() && value.length() <= 50 && value.matches("[A-Za-z0-9 ]+");
    }

    public String getValue() {
        return value;
    }

    @Override public String toString() {
        return value;
    }

    @Override public boolean equals(Object other) {
        return other instanceof AppointmentName name && value.equals(name.value);
    }

    @Override public int hashCode() {
        return value.hashCode();
    }
}
