package seedu.address.commons.exceptions;

/** Signals duplicate client names in saved data, without exposing client details. */
public class DuplicateClientNameException extends IllegalValueException {
    public static final String MESSAGE = "Persons list contains duplicate person(s).";

    public DuplicateClientNameException() {
        super(MESSAGE);
    }
}
