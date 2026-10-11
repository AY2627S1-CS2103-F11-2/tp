package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;

/**
 * Tests client references and invalid arguments for the delete command.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "C1", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \tC1 \n", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "C123", new DeleteCommand(Index.fromOneBased(123)));
        assertParseSuccess(parser, "C2147483647", new DeleteCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        String[] invalidArgs = {"", " ", "1", "C", "C0", "C-2", "C3.5", "Cabc", "C+1", "c1",
            "C 1", "C\t1", "C1 extra", "C1 C2", "C1 n/Alice", "C2147483648", "C99999999999999999999"};
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }
}
