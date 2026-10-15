package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditAppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class EditAppointmentCommandParserTest {
    private final EditAppointmentCommandParser parser = new EditAppointmentCommandParser();

    @Test
    public void parse_validInput_preservesLocationAndRoutesCommand() throws Exception {
        EditAppointmentCommand expected = new EditAppointmentCommand(INDEX_FIRST_PERSON, "NUS  COM3 #02-01");
        assertEquals(expected, parser.parse(" A1 loc/  NUS  COM3 #02-01  "));
        assertEquals(expected, parser.parse(" A1\tloc/NUS  COM3 #02-01"));
        assertEquals(expected, new AddressBookParser().parseCommand("editappt A1 loc/NUS  COM3 #02-01"));
    }

    @Test
    public void parse_invalidInput_rejects() {
        for (String args : new String[]{"", "A1", "A1 loc/", "A1 loc/   ", "A1 loc/\u2003",
            "1 loc/NUS", "a1 loc/NUS", "A0 loc/NUS", "A-1 loc/NUS", "A2147483648 loc/NUS",
            "A1 A2 loc/NUS", "A1 loc/NUS loc/COM3", "A1 loc/NUS n/New Name", "A1 x/NUS"}) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
