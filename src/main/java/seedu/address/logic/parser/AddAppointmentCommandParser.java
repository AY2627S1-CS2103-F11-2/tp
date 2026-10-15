package seedu.address.logic.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddAppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;

/** Parses the supporting appointment creation command with strict calendar validation. */
public class AddAppointmentCommandParser implements Parser<AddAppointmentCommand> {
    public static final String MESSAGE_FORMAT = "Invalid command format. Use: " + AddAppointmentCommand.MESSAGE_USAGE;
    public static final String MESSAGE_DATE_TIME = "Enter a valid Singapore date-time in YYYYMMDD-HHMM format.";
    private static final Prefix NAME = new Prefix("n/");
    private static final Prefix CLIENT = new Prefix("c/");
    private static final Prefix LOCATION = new Prefix("loc/");
    private static final Prefix START = new Prefix("s/");
    private static final Prefix END = new Prefix("e/");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("uuuuMMdd-HHmm").withResolverStyle(ResolverStyle.STRICT);

    @Override
    public AddAppointmentCommand parse(String args) throws ParseException {
        // The existing tokenizer recognizes space-delimited prefixes; also accept tabs at field boundaries.
        String normalized = " " + args.trim().replaceAll("[\\t\\r\\n]", " ");
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(normalized, NAME, CLIENT, LOCATION, START, END);
        if (!fields.getPreamble().isEmpty()
                || Stream.of(NAME, CLIENT, LOCATION, START, END).anyMatch(p -> fields.getValue(p).isEmpty())) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        fields.verifyNoDuplicatePrefixesFor(NAME, CLIENT, LOCATION, START, END);
        Matcher prefixes = Pattern.compile("(?<!\\S)([A-Za-z]+/)").matcher(normalized);
        while (prefixes.find()) {
            if (!Stream.of(NAME, CLIENT, LOCATION, START, END)
                    .anyMatch(p -> p.getPrefix().equals(prefixes.group(1)))) {
                throw new ParseException(MESSAGE_FORMAT);
            }
        }
        String name = fields.getValue(NAME).orElseThrow();
        if (!Appointment.isValidName(name)) {
            throw new ParseException(Appointment.MESSAGE_INVALID_NAME);
        }
        String location = ParserUtil.parseAppointmentLocation(fields.getValue(LOCATION).orElseThrow());
        Index client = AppointmentsCommandParser.parseClientIndex(fields.getValue(CLIENT).orElseThrow());
        LocalDateTime start = parseDateTime(fields.getValue(START).orElseThrow());
        LocalDateTime end = parseDateTime(fields.getValue(END).orElseThrow());
        if (!end.isAfter(start)) {
            throw new ParseException(Appointment.MESSAGE_INVALID_PERIOD);
        }
        return new AddAppointmentCommand(name, client, location, start, end);
    }

    private LocalDateTime parseDateTime(String value) throws ParseException {
        if (!value.matches("[0-9]{8}-[0-9]{4}")) {
            throw new ParseException(MESSAGE_DATE_TIME);
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_DATE_TIME);
        }
    }
}
