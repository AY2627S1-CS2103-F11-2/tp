package seedu.address.model.exceptions;

/**
 * Signals that there is no undone address book state to restore.
 */
public class NoRedoableStateException extends RuntimeException {
    public NoRedoableStateException() {
        super("There is no undone state to redo.");
    }
}
