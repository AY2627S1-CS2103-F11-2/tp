package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteAppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Requires exactly one displayed appointment reference and one confirmation word. */
public class DeleteAppointmentCommandParser implements Parser<DeleteAppointmentCommand> {
    public static final String MESSAGE_FORMAT =
            "Invalid command format. Use: " + DeleteAppointmentCommand.MESSAGE_USAGE;

    @Override
    public DeleteAppointmentCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty()) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        String[] words = trimmed.split("\\s+");
        if (words.length > 2) {
            throw new ParseException(MESSAGE_FORMAT);
        }
        Index index = ParserUtil.parseAppointmentIndex(words[0]);
        ParserUtil.parseConfirmation(words.length == 2 ? words[1] : "");
        return new DeleteAppointmentCommand(index);
    }
}
