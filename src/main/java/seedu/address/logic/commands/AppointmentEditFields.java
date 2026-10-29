package seedu.address.logic.commands;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDateTime;

import seedu.address.commons.core.index.Index;
import seedu.address.model.appointment.AppointmentName;

/**
 * Validated optional appointment changes. Null means omitted; an empty description clears the notes.
 * A client reference is a position in the displayed client list, resolved only during execution.
 */
public record AppointmentEditFields(String name, Index clientIndex, LocalDateTime start,
        String location, String description) {
    public static final String MESSAGE_INVALID_NAME = AppointmentName.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_INVALID_LOCATION = "Location cannot be blank if the loc/ prefix is supplied.";
    public static final String MESSAGE_LOCATION_TOO_LONG = "Location must contain at most 100 characters.";
    public static final String MESSAGE_INVALID_DATE_TIME = "Invalid date-time format! Date and time must follow "
            + "'yyyy-MM-dd HH:mm' and must be a valid calendar time.";
    public static final String MESSAGE_NO_FIELDS = "At least one field to edit must be provided: "
            + "[n/APPOINTMENT_NAME] [c/CLIENT_INDEX] [d/DATETIME] [loc/LOCATION] [desc/DESCRIPTION]";

    /** Validates every supplied change before the model can be mutated. */
    public AppointmentEditFields {
        checkArgument(name != null || clientIndex != null || start != null || location != null || description != null,
                MESSAGE_NO_FIELDS);
        if (name != null) {
            name = name.strip();
            checkArgument(AppointmentName.isValid(name), MESSAGE_INVALID_NAME);
        }
        if (start != null) {
            checkArgument(start.getYear() >= 1 && start.getYear() <= 9999
                    && start.getSecond() == 0 && start.getNano() == 0, MESSAGE_INVALID_DATE_TIME);
        }
        if (location != null) {
            location = location.strip();
            checkArgument(!location.isEmpty(), MESSAGE_INVALID_LOCATION);
            checkArgument(location.length() <= 100, MESSAGE_LOCATION_TOO_LONG);
        }
    }
}
