package seedu.address.model;

import static java.util.Objects.requireNonNull;

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
        addressBookStateList.add(new AddressBook(this));
        currentStatePointer = 0;
    }

    /** Restores data while keeping previously allocated IDs unavailable to future clients. */
    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);
        long highWaterMark = Math.max(getNextClientId(), newData.getNextClientId());
        super.resetData(newData);
        setNextClientId(Math.max(highWaterMark, getNextClientId()));
    }

    /**
     * Saves the current state in the history so that it can be returned to with {@code undo()}.
     * A state identical to the one already at the pointer is not saved, which is what keeps commands that
     * do not change data out of the history.
     */
    public void commit() {
        AddressBook historyState = new AddressBook(addressBookStateList.get(currentStatePointer));
        AddressBook currentState = new AddressBook(this);
        long highWaterMark = Math.max(historyState.getNextClientId(), getNextClientId());
        historyState.setNextClientId(highWaterMark);
        currentState.setNextClientId(highWaterMark);
        if (historyState.equals(currentState)) {
            return;
        }
        addressBookStateList.subList(currentStatePointer + 1, addressBookStateList.size()).clear();
        addressBookStateList.add(new AddressBook(this));
        currentStatePointer++;
    }

    /**
     * Makes the current data a new history baseline after an irreversible operation.
     * Neither undo nor redo can cross this boundary.
     */
    public void resetHistory() {
        AddressBook baseline = new AddressBook(this);
        addressBookStateList.clear();
        addressBookStateList.add(baseline);
        currentStatePointer = 0;
    }

    /** Captures data and the complete history so a failed save can be rolled back. */
    State captureState() {
        return new State(new AddressBook(this), List.copyOf(addressBookStateList), currentStatePointer);
    }

    /** Restores a captured state without recording a new history entry. */
    void restoreState(State state) {
        requireNonNull(state);
        super.resetData(state.data);
        addressBookStateList.clear();
        addressBookStateList.addAll(state.history);
        currentStatePointer = state.pointer;
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

    /** History snapshots remain private and are never modified after capture. */
    static final class State {
        private final AddressBook data;
        private final List<ReadOnlyAddressBook> history;
        private final int pointer;

        private State(AddressBook data, List<ReadOnlyAddressBook> history, int pointer) {
            this.data = data;
            this.history = history;
            this.pointer = pointer;
        }
    }
}
