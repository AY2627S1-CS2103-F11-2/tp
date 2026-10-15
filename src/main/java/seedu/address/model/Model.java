package seedu.address.model;

import java.time.LocalDate;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.exceptions.NoRedoableStateException;
import seedu.address.model.exceptions.NoUndoableStateException;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /** Opaque, in-memory state belonging to one model, used to recover from a failed save. */
    interface Checkpoint {}

    /** Captures current data, history and displayed filters before command execution. */
    Checkpoint createCheckpoint();

    /** Restores this model's checkpoint without recording a new change. */
    void restoreCheckpoint(Checkpoint checkpoint);

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Returns true if there is an earlier state of the address book to return to.
     */
    boolean canUndoAddressBook();

    /**
     * Returns true if there is an undone state of the address book to restore.
     */
    boolean canRedoAddressBook();

    /**
     * Restores the address book to the state it had before the last change to its data.
     *
     * @throws NoUndoableStateException if there is no earlier state to restore.
     */
    void undoAddressBook();

    /**
     * Restores the address book state that was undone most recently.
     *
     * @throws NoRedoableStateException if there is no undone state to restore.
     */
    void redoAddressBook();

    /** Returns chronologically sorted, unmodifiable appointment results. */
    ObservableList<Appointment> getFilteredAppointmentList();

    /** Replaces the appointment filter; null client/date means no restriction for that field. */
    void updateFilteredAppointmentList(Person client, LocalDate date);

    /** Adds an appointment linked to an existing client. */
    void addAppointment(Appointment appointment);

    /**
     * Atomically replaces an existing appointment and records one undo step if data changes.
     * Invalid replacements leave data, filters and history unchanged. Future editappt commands should
     * construct the complete replacement and call this operation once.
     */
    void setAppointment(Appointment target, Appointment editedAppointment);

    /** Deletes an existing appointment, clears undo/redo history and shows all remaining appointments. */
    void deleteAppointment(Appointment appointment);
}
