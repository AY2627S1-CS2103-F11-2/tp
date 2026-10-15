package seedu.address.model;

import java.util.ArrayList;
import java.util.List;

import seedu.address.model.exceptions.NoRedoableStateException;
import seedu.address.model.exceptions.NoUndoableStateException;

/**
 * An {@code AddressBook} that keeps a history of its own states, so that changes can be undone and redone.
 */
public class VersionedAddressBook extends AddressBook {

    private final List<ReadOnlyAddressBook> addressBookStateList = new ArrayList<>();
    private int currentStatePointer;

    /**
     * Creates a versioned address book whose history starts with the given initial state.
     */
    public VersionedAddressBook(ReadOnlyAddressBook initialState) {
        super(initialState);
        addressBookStateList.add(new AddressBook(initialState));
        currentStatePointer = 0;
    }

    /**
     * Saves the current state in the history so that it can be returned to with {@code undo()}.
     * A state identical to the one already at the pointer is not saved, which is what keeps commands that
     * do not change data out of the history.
     */
    public void commit() {
        if (addressBookStateList.get(currentStatePointer).equals(this)) {
            return;
        }
        addressBookStateList.subList(currentStatePointer + 1, addressBookStateList.size()).clear();
        addressBookStateList.add(new AddressBook(this));
        currentStatePointer++;
    }

    /**
     * Restores the previous state from the history.
     *
     * @throws NoUndoableStateException if there is no earlier state to restore.
     */
    public void undo() {
        if (!canUndo()) {
            throw new NoUndoableStateException();
        }
        currentStatePointer--;
        resetData(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Restores the state that was undone most recently.
     *
     * @throws NoRedoableStateException if there is no undone state to restore.
     */
    public void redo() {
        if (!canRedo()) {
            throw new NoRedoableStateException();
        }
        currentStatePointer++;
        resetData(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Returns true if there is an earlier state to return to.
     */
    public boolean canUndo() {
        return currentStatePointer > 0;
    }

    /**
     * Returns true if there is an undone state to restore.
     */
    public boolean canRedo() {
        return currentStatePointer < addressBookStateList.size() - 1;
    }
}
