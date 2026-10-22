package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the required backup file name for a restore and rejects any other field. */
public class RestoreCommandParser implements Parser<RestoreCommand> {

    public static final String MESSAGE_FORMAT =
            "Invalid command format. Use: " + RestoreCommand.MESSAGE_USAGE;

    private static final Prefix FILE = new Prefix("f/");
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([A-Za-z]+/)");

    @Override
    public RestoreCommand parse(String args) throws ParseException {
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

        String name = fields.getValue(FILE).orElse("").strip();
        if (name.isEmpty()) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        return new RestoreCommand(name);
    }
}
