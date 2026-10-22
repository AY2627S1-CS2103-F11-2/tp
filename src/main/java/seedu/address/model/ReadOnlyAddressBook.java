package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns the next unused client ID. Implementations that do not store allocation metadata
     * derive it from existing IDs; persistent address books also retain IDs of deleted clients.
     */
    default long getNextClientId() {
        return getPersonList().stream()
                .flatMap(person -> person.getClientId().stream())
                .mapToLong(id -> id.getNumber() + 1)
                .max().orElse(1);
    }

    /** Returns an unmodifiable view of all stored appointments. */
    ObservableList<Appointment> getAppointmentList();

}
