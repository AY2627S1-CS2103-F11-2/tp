package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Value object representing an appointment name.
 */
public final class AppointmentName {
    public static final int MAX_LENGTH = 80;
    public static final String MESSAGE_CONSTRAINTS = "Appointment name must contain only letters, digits and "
            + "spaces, with 1-80 characters and at least one letter or digit.";
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

    /**
     * Returns whether the supplied value is a valid appointment name. Creating and editing an appointment
     * share this rule, so any name one command accepts the other accepts too. Names valid under the
     * earlier, narrower creation and editing rules all remain valid.
     */
    public static boolean isValid(String value) {
        return value != null && value.length() <= MAX_LENGTH
                && value.matches("[A-Za-z0-9 ]+") && value.matches(".*[A-Za-z0-9].*");
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
