package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArgs_returnsRemarkCommand() {
        assertParseSuccess(parser, " 1 r/Likes baseball",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball")));
        assertParseSuccess(parser, "  1   r/  Call after 6 pm!  ",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Call after 6 pm!")));
        assertParseSuccess(parser, " 1 r/客户; y/buyer",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("客户; y/buyer")));
    }

    @Test
    public void parse_emptyOrAbsentRemark_clearsRemark() {
        RemarkCommand expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, " 1 r/", expectedCommand);
        assertParseSuccess(parser, " 1 r/   ", expectedCommand);
        assertParseSuccess(parser, " 1", expectedCommand);
    }

    @Test
    public void parse_repeatedRemarkPrefix_usesLastValue() {
        assertParseSuccess(parser, " 1 r/Old note r/New note",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("New note")));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " r/New note", expectedMessage);
        assertParseFailure(parser, " 0 r/New note", expectedMessage);
        assertParseFailure(parser, " -1 r/New note", expectedMessage);
        assertParseFailure(parser, " a r/New note", expectedMessage);
        assertParseFailure(parser, " 1 2 r/New note", expectedMessage);
        assertParseFailure(parser, " 2147483648 r/New note", expectedMessage);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
