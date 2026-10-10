package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Type;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INVALID_APPOINTMENT_INDEX =
            "Appointment index must be A followed by a positive integer, e.g. A1.";
    public static final String MESSAGE_MISSING_CONFIRMATION = "Deletion requires yes/confirm. Nothing was deleted.";
    public static final String MESSAGE_EMPTY_DATE = "DATE cannot be empty.";
    public static final String MESSAGE_INVALID_DATE = "Enter a valid date in DDMMYYYY format.";

    private static final String APPOINTMENT_INDEX_PREFIX = "A";
    private static final Set<String> CONFIRMATION_WORDS = Set.of("yes", "confirm");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("ddMMuuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code appointmentIndex} of the form {@code A<index>} (e.g. {@code A1}) into an {@code Index} and
     * returns it. Leading and trailing whitespaces will be trimmed.
     * @throws ParseException if the specified appointment index is invalid.
     */
    public static Index parseAppointmentIndex(String appointmentIndex) throws ParseException {
        requireNonNull(appointmentIndex);
        String trimmedIndex = appointmentIndex.trim();
        if (!trimmedIndex.startsWith(APPOINTMENT_INDEX_PREFIX)) {
            throw new ParseException(MESSAGE_INVALID_APPOINTMENT_INDEX);
        }
        String number = trimmedIndex.substring(APPOINTMENT_INDEX_PREFIX.length());
        if (!StringUtil.isNonZeroUnsignedInteger(number)) {
            throw new ParseException(MESSAGE_INVALID_APPOINTMENT_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(number));
    }

    /**
     * Checks that {@code confirmation} is exactly {@code yes} or {@code confirm}.
     * Leading and trailing whitespaces will be trimmed.
     * @throws ParseException if the confirmation word is missing or is not {@code yes} or {@code confirm}.
     */
    public static void parseConfirmation(String confirmation) throws ParseException {
        requireNonNull(confirmation);
        if (!CONFIRMATION_WORDS.contains(confirmation.trim())) {
            throw new ParseException(MESSAGE_MISSING_CONFIRMATION);
        }
    }

    /**
     * Parses a {@code String date} in DDMMYYYY format (e.g. {@code 15102026}) into a {@code LocalDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code date} is empty, not in DDMMYYYY format, or not a calendar date.
     */
    public static LocalDate parseDate(String date) throws ParseException {
        requireNonNull(date);
        String trimmedDate = date.trim();
        if (trimmedDate.isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_DATE);
        }
        if (!trimmedDate.matches("\\d{8}")) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }
        try {
            return LocalDate.parse(trimmedDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String type} into an {@code Type}
     * Leading and trailing whitespaces will be trimmed
     *
     * @throws ParseException if the given {@code type} is invalid.
     */
    public static Type parseType(String type) throws ParseException {
        requireNonNull(type);
        String trimmedType = type.trim();
        if (!Type.isValidType(trimmedType)) {
            throw new ParseException(Type.MESSAGE_CONSTRAINTS);
        }
        return new Type(trimmedType);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
