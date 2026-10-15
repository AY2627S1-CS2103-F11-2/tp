package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
        appointments.removeIf(appointment -> !persons.contains(appointment.getClient()));
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        // Validate and copy before mutating, including when resetting from this address book itself.
        UniquePersonList replacementPersons = new UniquePersonList();
        replacementPersons.setPersons(newData.getPersonList());
        List<Appointment> replacementAppointments = List.copyOf(newData.getAppointmentList());
        if (replacementAppointments.stream().anyMatch(a -> !newData.getPersonList().contains(a.getClient()))
                || replacementAppointments.stream().distinct().count() != replacementAppointments.size()) {
            throw new IllegalArgumentException("Invalid appointment client or duplicate appointment.");
        }
        persons.setPersons(replacementPersons);
        appointments.setAll(replacementAppointments);
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getClient().equals(target)) {
                appointments.set(i, appointments.get(i).withClient(editedPerson));
            }
        }
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
        appointments.removeIf(appointment -> appointment.getClient().equals(key));
    }

    /** Adds an appointment linked to an existing client, rejecting schedule clashes and duplicates. */
    public void addAppointment(Appointment appointment) {
        requireNonNull(appointment);
        validateAppointment(appointment, -1);
        appointments.add(appointment);
    }

    /**
     * Replaces one existing appointment after validating the complete replacement.
     * The linked client must exist, and the appointment must not clash with any other appointment.
     * Identical replacements do not emit a list change.
     */
    public void setAppointment(Appointment target, Appointment editedAppointment) {
        requireNonNull(target);
        requireNonNull(editedAppointment);
        int targetIndex = appointments.indexOf(target);
        if (targetIndex < 0) {
            throw new IllegalArgumentException("Appointment does not exist.");
        }
        validateAppointment(editedAppointment, targetIndex);
        if (!target.equals(editedAppointment)) {
            appointments.set(targetIndex, editedAppointment);
        }
    }

    private void validateAppointment(Appointment appointment, int excludedIndex) {
        if (!getPersonList().contains(appointment.getClient())) {
            throw new IllegalArgumentException("Appointment client does not exist.");
        }
        for (int i = 0; i < appointments.size(); i++) {
            if (i != excludedIndex && appointment.overlaps(appointments.get(i))) {
                throw new IllegalArgumentException("Schedule clash with appointment: " + appointments.get(i).getName());
            }
        }
    }

    /** Removes an existing appointment. */
    public void removeAppointment(Appointment appointment) {
        requireNonNull(appointment);
        if (!appointments.remove(appointment)) {
            throw new IllegalArgumentException("Appointment does not exist.");
        }
    }

    @Override
    public ObservableList<Appointment> getAppointmentList() {
        return FXCollections.unmodifiableObservableList(appointments);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons) && appointments.equals(otherAddressBook.appointments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, appointments);
    }
}
