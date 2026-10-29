package seedu.address.logic.parser;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AppointmentsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses optional, single-valued client and date filters. */
public class AppointmentsCommandParser implements Parser<AppointmentsCommand> {
    public static final String MESSAGE_FORMAT = "Invalid command format. Use: " + AppointmentsCommand.MESSAGE_USAGE;
    public static final String MESSAGE_DUPLICATE = "Each parameter may be specified only once.";
    public static final String MESSAGE_CLIENT_INDEX = "Client reference must be an uppercase C followed by the "
            + "client's position in the displayed client list, for example c/C1 for the first client.";
    public static final String MESSAGE_EMPTY_CLIENT = "CLIENT_INDEX cannot be empty.";

    private static final Prefix CLIENT = new Prefix("c/");
    private static final Prefix DATE = new Prefix("d/");

    @Override
    public AppointmentsCommand parse(String args) throws ParseException {
        String normalized = " " + args.trim().replaceAll("\\s+", " ");
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(normalized, CLIENT, DATE);
        if (!fields.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        if (fields.getAllValues(CLIENT).size() > 1 || fields.getAllValues(DATE).size() > 1) {
            throw new ParseException(MESSAGE_DUPLICATE);
        }
        // Unknown fields must not be swallowed as part of another field's value.
        Matcher prefixes = Pattern.compile("(?<!\\S)([A-Za-z]+/)").matcher(normalized);
        while (prefixes.find()) {
            if (!prefixes.group(1).equals("c/") && !prefixes.group(1).equals("d/")) {
                throw new ParseException(MESSAGE_FORMAT);
            }
        }
        if (fields.getValue(CLIENT).orElse("").contains(" ")
                || fields.getValue(DATE).orElse("").contains(" ")) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        Index client = fields.getValue(CLIENT).isPresent() ? parseClientIndex(fields.getValue(CLIENT).get()) : null;
        LocalDate date = fields.getValue(DATE).isPresent() ? ParserUtil.parseDate(fields.getValue(DATE).get()) : null;
        return new AppointmentsCommand(client, date);
    }

    /** Parses a reference to the currently displayed client list. */
    public static Index parseClientIndex(String value) throws ParseException {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_CLIENT);
        }
        if (!trimmed.matches("C[1-9][0-9]*")) {
            throw new ParseException(MESSAGE_CLIENT_INDEX);
        }
        try {
            return ParserUtil.parseIndex(trimmed.substring(1));
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_CLIENT_INDEX);
        }
    }
}
