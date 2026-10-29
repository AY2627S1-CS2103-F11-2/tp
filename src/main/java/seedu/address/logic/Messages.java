package seedu.address.logic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX = "The client index provided is invalid.";
    public static final String MESSAGE_CLIENTS_LISTED_OVERVIEW = "Found %1$d client(s)!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns the message shown at startup when the data file could not be read, naming the file and,
     * when the rename succeeded, where the unreadable file went.
     */
    public static String formatUnreadableDataFile(Path dataFile, Optional<Path> quarantinedFile) {
        String message = String.format(
                "Your data file at %s could not be read, so the app started with an empty list.", dataFile);
        return quarantinedFile
                .map(path -> message + String.format(
                        " The unreadable file was renamed to %s so it is not overwritten.", path))
                .orElse(message + " The file could not be renamed, so check it before making changes.");
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail().getDisplayValue())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Type: ")
                .append(person.getType())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

}
