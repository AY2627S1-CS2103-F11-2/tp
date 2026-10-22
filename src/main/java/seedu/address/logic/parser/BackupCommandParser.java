package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an optional backup file name and rejects any other field. */
public class BackupCommandParser implements Parser<BackupCommand> {

    public static final String MESSAGE_FORMAT =
            "Invalid command format. Use: " + BackupCommand.MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_FILE =
            "Backup file should be a .json file name, for example f/before-july.json.";

    private static final Prefix FILE = new Prefix("f/");
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([A-Za-z]+/)");
    private static final Pattern INVALID_CHARS = Pattern.compile("[<>:\"|?*\\p{Cntrl}]");

    @Override
    public BackupCommand parse(String args) throws ParseException {
        String normalized = " " + requireNonNull(args).trim();
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(normalized, FILE);

        Matcher prefixes = FIELD_PREFIX.matcher(normalized);
        while (prefixes.find()) {
            if (!FILE.getPrefix().equals(prefixes.group(1))) {
                throw new ParseException(MESSAGE_FORMAT);
            }
        }
        if (!fields.getPreamble().isBlank()) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        fields.verifyNoDuplicatePrefixesFor(FILE);

        Optional<String> value = fields.getValue(FILE);
        if (value.isEmpty()) {
            if (FIELD_PREFIX.matcher(normalized).find()) {
                throw new ParseException(MESSAGE_INVALID_FILE);
            }
            return new BackupCommand(Optional.empty());
        }
        String name = value.get().strip();
        if (!isValidFileName(name)) {
            throw new ParseException(MESSAGE_INVALID_FILE);
        }
        return new BackupCommand(Optional.of(name));
    }

    /** A backup name is a bare file name ending in .json, with no folder part and no control characters. */
    private static boolean isValidFileName(String name) {
        return !name.isEmpty()
                && name.endsWith(".json")
                && !name.contains("/")
                && !name.contains("\\")
                && !name.contains("..")
                && !INVALID_CHARS.matcher(name).find();
    }
}
