package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    private static final Logger logger = LogsCenter.getLogger(DeleteCommandParser.class);

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws NullPointerException if {@code args} is null
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String clientId = args.trim();
        if (!clientId.matches("C[1-9][0-9]*")) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
        try {
            Index index = ParserUtil.parseIndex(clientId.substring(1));
            return new DeleteCommand(index);
        } catch (ParseException pe) {
            logger.fine("Client deletion rejected: invalid client reference.");
            throw new ParseException(DeleteCommand.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX, pe);
        }
    }

}
