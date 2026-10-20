package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.ClientId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Type;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final JsonNode clientId;
    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String type;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("type") String type, @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("clientId") JsonNode clientId) {
        this.clientId = clientId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.type = type;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /** Creates an adapted legacy client without a stored identifier. */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            String type, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, type, tags, null);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        clientId = source.getClientId().map(id -> LongNode.valueOf(id.getNumber())).orElse(null);
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        type = source.getType().clientType;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email != null && !email.isEmpty() && !Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = email == null || email.isEmpty() ? Email.empty() : new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        if (type == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Type.class.getSimpleName()));
        }
        if (!Type.isValidType(type)) {
            throw new IllegalValueException(Type.MESSAGE_CONSTRAINTS);
        }
        final Type modelType = new Type(type);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        Person person = new Person(modelName, modelPhone, modelEmail, modelAddress, modelType, modelTags);
        if (clientId == null || clientId.isNull()) {
            return person;
        }
        if (!clientId.isIntegralNumber() || !clientId.canConvertToLong()
                || !ClientId.isValidNumber(clientId.longValue())) {
            throw new IllegalValueException(ClientId.MESSAGE_CONSTRAINTS);
        }
        return person.withClientId(new ClientId(clientId.longValue()));
    }

}
