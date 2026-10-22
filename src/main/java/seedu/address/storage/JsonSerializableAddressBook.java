package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    private final JsonNode nextClientId;
    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedAppointment> appointments = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("appointments") List<JsonAdaptedAppointment> appointments,
            @JsonProperty("nextClientId") JsonNode nextClientId) {
        this.nextClientId = nextClientId;
        this.persons.addAll(requireNonNull(persons));
        if (appointments != null) {
            this.appointments.addAll(appointments);
        }
    }

    /** Creates an adapted legacy address book without allocation metadata. */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons, List<JsonAdaptedAppointment> appointments) {
        this(persons, appointments, null);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        nextClientId = LongNode.valueOf(source.getNextClientId());
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        source.getAppointmentList().stream()
                .map(a -> new JsonAdaptedAppointment(a, source.getPersonList())).forEach(appointments::add);
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        List<Person> modelPersons = new ArrayList<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException("Client entry cannot be null.");
            }
            Person person = jsonAdaptedPerson.toModelType();
            if (modelPersons.stream().anyMatch(person::isSamePerson)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            modelPersons.add(person);
        }
        try {
            if (nextClientId != null && !nextClientId.isNull()) {
                if (!nextClientId.isIntegralNumber() || !nextClientId.canConvertToLong()
                        || nextClientId.longValue() < 1) {
                    throw new IllegalArgumentException("Next client ID must be a positive integer.");
                }
                long highestId = modelPersons.stream().flatMap(person -> person.getClientId().stream())
                        .mapToLong(id -> id.getNumber()).max().orElse(0);
                if (nextClientId.longValue() <= highestId) {
                    throw new IllegalArgumentException("Next client ID must exceed every stored client ID.");
                }
                addressBook.setNextClientId(nextClientId.longValue());
            }
            addressBook.setPersons(modelPersons);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
        for (JsonAdaptedAppointment jsonAppointment : appointments) {
            if (jsonAppointment == null) {
                throw new IllegalValueException("Appointment entry cannot be null.");
            }
            Appointment appointment = jsonAppointment.toModelType(addressBook.getPersonList());
            try {
                addressBook.addAppointment(appointment);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage());
            }
        }
        return addressBook;
    }

}
