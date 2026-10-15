package seedu.address.model.exceptions;

/**
 * Signals that there is no earlier address book state to return to.
 */
public class NoUndoableStateException extends RuntimeException {
    public NoUndoableStateException() {
        super("There is no earlier state to undo.");
    }
}
