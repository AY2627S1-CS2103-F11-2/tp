package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AppointmentEditFields;
import seedu.address.logic.commands.EditAppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a displayed appointment position and the five optional fields from the feature specification. */
public class EditAppointmentCommandParser implements Parser<EditAppointmentCommand> {
    public static final String MESSAGE_FORMAT = "Invalid command format! editappt: Edits the details of the "
            + "appointment identified by the index number used in the displayed appointment list.\nUse: "
            + EditAppointmentCommand.MESSAGE_USAGE;
    private static final Logger logger = LogsCenter.getLogger(EditAppointmentCommandParser.class);
    private static final Prefix NAME = new Prefix("n/");
    private static final Prefix CLIENT = new Prefix("c/");
    private static final Prefix DATE_TIME = new Prefix("d/");
    private static final Prefix LOCATION = new Prefix("loc/");
    private static final Prefix DESCRIPTION = new Prefix("desc/");
    private static final Prefix[] EDITABLE_PREFIXES = {NAME, CLIENT, DATE_TIME, LOCATION, DESCRIPTION};
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([A-Za-z][A-Za-z0-9_-]*/)");

    @Override
    public EditAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            String normalized = " " + args.strip().replaceAll("[\\t\\r\\n](?=(?:n|c|d|loc|desc)/)", " ");
            ArgumentMultimap fields = ArgumentTokenizer.tokenize(normalized, EDITABLE_PREFIXES);
            if (fields.getPreamble().isEmpty()) {
                throw new ParseException(MESSAGE_FORMAT);
            }
            Matcher prefixes = FIELD_PREFIX.matcher(normalized);
            while (prefixes.find()) {
                if (Stream.of(EDITABLE_PREFIXES).noneMatch(p -> p.getPrefix().equals(prefixes.group(1)))) {
                    throw new ParseException(MESSAGE_FORMAT);
                }
            }
            fields.verifyNoDuplicatePrefixesFor(EDITABLE_PREFIXES);
            Index index = parseDisplayedIndex(fields.getPreamble(), "appointment");
            if (Stream.of(EDITABLE_PREFIXES).noneMatch(p -> fields.getValue(p).isPresent())) {
                throw new ParseException(AppointmentEditFields.MESSAGE_NO_FIELDS);
            }
            Index client = fields.getValue(CLIENT).isPresent()
                    ? parseDisplayedIndex(fields.getValue(CLIENT).orElseThrow(), "client") : null;
            AppointmentEditFields edits = new AppointmentEditFields(fields.getValue(NAME).orElse(null), client,
                    fields.getValue(DATE_TIME).isPresent()
                            ? ParserUtil.parseAppointmentEditDateTime(fields.getValue(DATE_TIME).orElseThrow()) : null,
                    fields.getValue(LOCATION).orElse(null), fields.getValue(DESCRIPTION).orElse(null));
            return new EditAppointmentCommand(index, edits);
        } catch (IllegalArgumentException e) {
            logger.fine("Appointment edit rejected: invalid field value.");
            throw new ParseException(e.getMessage(), e);
        } catch (ParseException e) {
            logger.fine("Appointment edit rejected during parsing.");
            throw e;
        }
    }

    private Index parseDisplayedIndex(String value, String kind) throws ParseException {
        String message = "The " + kind + " index provided is invalid. Use a positive integer from the displayed list.";
        if (!value.matches("[0-9]+")) {
            throw new ParseException(message);
        }
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException e) {
            throw new ParseException(message, e);
        }
    }
}
