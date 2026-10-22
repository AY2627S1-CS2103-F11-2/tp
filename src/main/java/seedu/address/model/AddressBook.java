package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.ClientId;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private long nextClientId = 1;

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
        List<Person> replacementPersons = assignMissingClientIds(persons, nextClientId);
        this.persons.setPersons(replacementPersons);
        nextClientId = nextIdAfter(replacementPersons, nextClientId);
        appointments.removeIf(appointment -> !replacementPersons.contains(appointment.getClient()));
        appointments.replaceAll(appointment -> appointment.withClient(findStoredPerson(appointment.getClient())));
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        // Validate and copy before mutating, including when resetting from this address book itself.
        UniquePersonList replacementPersons = new UniquePersonList();
        long replacementNextId = newData.getNextClientId();
        validateNextClientId(replacementNextId, newData.getPersonList());
        List<Person> identifiedPersons = assignMissingClientIds(newData.getPersonList(), replacementNextId);
        replacementPersons.setPersons(identifiedPersons);
        List<Appointment> replacementAppointments = new ArrayList<>();
        for (Appointment appointment : newData.getAppointmentList()) {
            Person storedClient = identifiedPersons.stream().filter(appointment.getClient()::equals)
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid appointment client."));
            replacementAppointments.add(appointment.withClient(storedClient));
        }
        if (replacementAppointments.stream().anyMatch(a -> !newData.getPersonList().contains(a.getClient()))
                || replacementAppointments.stream().distinct().count() != replacementAppointments.size()) {
            throw new IllegalArgumentException("Invalid appointment client or duplicate appointment.");
        }
        persons.setPersons(replacementPersons);
        appointments.setAll(replacementAppointments);
        nextClientId = nextIdAfter(identifiedPersons, replacementNextId);
    }

    /** Returns the persistent allocation counter, including identifiers of removed clients. */
    @Override
    public long getNextClientId() {
        return nextClientId;
    }

    /** Restores a persisted allocation counter after validating it against all current clients. */
    public void setNextClientId(long nextClientId) {
        validateNextClientId(nextClientId, getPersonList());
        this.nextClientId = nextClientId;
    }

    private static void validateNextClientId(long nextClientId, List<Person> persons) {
        if (nextClientId < 1 || persons.stream().flatMap(person -> person.getClientId().stream())
                .anyMatch(id -> id.getNumber() >= nextClientId)) {
            throw new IllegalArgumentException("Next client ID must be greater than every stored client ID.");
        }
    }

    private static long nextIdAfter(List<Person> persons, long firstUnusedId) {
        long highestSuccessor = persons.stream().flatMap(person -> person.getClientId().stream())
                .mapToLong(id -> id.getNumber() + 1).max().orElse(firstUnusedId);
        return Math.max(firstUnusedId, highestSuccessor);
    }

    /** Reserves explicit IDs first, then migrates older clients in list order without collisions. */
    private static List<Person> assignMissingClientIds(List<Person> source, long firstUnusedId) {
        requireNonNull(source);
        Set<ClientId> allocated = new HashSet<>();
        for (Person person : source) {
            requireNonNull(person);
            if (person.getClientId().isPresent() && !allocated.add(person.getClientId().get())) {
                throw new IllegalArgumentException("Clients contain duplicate IDs.");
            }
        }
        long candidate = nextIdAfter(source, firstUnusedId);
        List<Person> identified = new ArrayList<>();
        for (Person person : source) {
            if (person.getClientId().isPresent()) {
                identified.add(person);
            } else {
                identified.add(person.withClientId(new ClientId(candidate++)));
            }
        }
        return identified;
    }

    private Person findStoredPerson(Person person) {
        return getPersonList().stream().filter(person::equals).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Client does not exist."));
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
        requireNonNull(p);
        Person identified = p.getClientId().isPresent() ? p : p.withClientId(new ClientId(nextClientId));
        if (getPersonList().stream().anyMatch(person -> person.getClientId().equals(identified.getClientId()))) {
            throw new IllegalArgumentException("Client ID already exists.");
        }
        persons.add(identified);
        nextClientId = Math.max(nextClientId, identified.getClientId().orElseThrow().getNumber() + 1);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        Person storedTarget = findStoredPerson(target);
        Person identifiedEdit = editedPerson.withClientId(storedTarget.getClientId().orElseThrow());
        persons.setPerson(target, identifiedEdit);
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getClient().equals(target)) {
                appointments.set(i, appointments.get(i).withClient(identifiedEdit));
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
        appointments.add(appointment.withClient(findStoredPerson(appointment.getClient())));
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
            appointments.set(targetIndex,
                    editedAppointment.withClient(findStoredPerson(editedAppointment.getClient())));
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

        return persons.equals(otherAddressBook.persons) && appointments.equals(otherAddressBook.appointments)
                && clientIds().equals(otherAddressBook.clientIds()) && nextClientId == otherAddressBook.nextClientId;
    }

    private List<ClientId> clientIds() {
        return getPersonList().stream().map(person -> person.getClientId().orElseThrow()).toList();
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, appointments, clientIds(), nextClientId);
    }
}
