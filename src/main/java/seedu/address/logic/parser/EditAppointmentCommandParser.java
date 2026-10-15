package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditAppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a displayed appointment reference and a single location field. */
public class EditAppointmentCommandParser implements Parser<EditAppointmentCommand> {
    public static final String MESSAGE_FORMAT = "Invalid command format. Use: " + EditAppointmentCommand.MESSAGE_USAGE;
    private static final Prefix LOCATION = new Prefix("loc/");
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([A-Za-z]+/)");

    @Override
    public EditAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String normalized = " " + args.trim().replaceAll("[\\t\\r\\n](?=loc/)", " ");
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(normalized, LOCATION);
        if (fields.getValue(LOCATION).isEmpty()) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        fields.verifyNoDuplicatePrefixesFor(LOCATION);
        Matcher prefixes = FIELD_PREFIX.matcher(normalized);
        while (prefixes.find()) {
            if (!LOCATION.getPrefix().equals(prefixes.group(1))) {
                throw new ParseException(MESSAGE_FORMAT);
            }
        }
        Index index = ParserUtil.parseAppointmentIndex(fields.getPreamble());
        String location = ParserUtil.parseAppointmentLocation(fields.getValue(LOCATION).orElseThrow());
        return new EditAppointmentCommand(index, location);
    }
}
