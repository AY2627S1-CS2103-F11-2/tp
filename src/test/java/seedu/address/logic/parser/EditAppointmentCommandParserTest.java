package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AppointmentEditFields;
import seedu.address.logic.commands.EditAppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class EditAppointmentCommandParserTest {
    private final EditAppointmentCommandParser parser = new EditAppointmentCommandParser();

    @Test
    public void parse_validInput_preservesLocationAndRoutesCommand() throws Exception {
        EditAppointmentCommand expected = new EditAppointmentCommand(INDEX_FIRST_PERSON, "NUS  COM3 #02-01");
        assertEquals(expected, parser.parse(" 1 loc/  NUS  COM3 #02-01  "));
        assertEquals(expected, parser.parse(" 1\tloc/NUS  COM3 #02-01"));
        assertEquals(expected, new AddressBookParser().parseCommand("editappt 1 loc/NUS  COM3 #02-01"));
    }

    @Test
    public void parse_allFields_acceptsAnyOrder() throws Exception {
        AppointmentEditFields fields = new AppointmentEditFields("Viewing 2", Index.fromOneBased(2),
                LocalDateTime.of(2026, 11, 1, 10, 0), "NUS  COM3");
        EditAppointmentCommand expected = new EditAppointmentCommand(INDEX_FIRST_PERSON, fields);
        assertEquals(expected, parser.parse("1 n/Viewing 2 c/2 d/2026-11-01 10:00 loc/NUS  COM3"));
        assertEquals(expected, parser.parse("1 loc/NUS  COM3 d/2026-11-01 10:00 c/2 n/Viewing 2"));
    }

    @Test
    public void parse_repeatedPrefixes_rejectsEvenIdenticalOrEmptyValues() {
        for (String fields : new String[]{"n/Viewing n/Viewing", "c/1 c/1", "d/2026-10-18 15:30 d/2026-10-18 15:30",
            "loc/NUS loc/NUS"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse("1 " + fields));
            assertEquals(true, error.getMessage().startsWith("Multiple values specified"));
        }
    }

    @Test
    public void parse_invalidInput_rejects() {
        for (String args : new String[]{"", "1", "1 loc/", "1 loc/   ", "1 loc/\u2003",
            "A1 loc/NUS", "0 loc/NUS", "-1 loc/NUS", "2147483648 loc/NUS", "1 extra loc/NUS",
            "1 x/NUS", "1 c/", "1 c/C1", "1 c/0", "1 c/2147483648", "1 n/", "1 n/Name!",
            "1 n/" + "A".repeat(51), "1 loc/" + "A".repeat(101), "1 d/", "1 d/2026-02-30 10:00"}) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
