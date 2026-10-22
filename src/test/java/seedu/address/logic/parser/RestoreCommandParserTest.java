package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class RestoreCommandParserTest {

    private final RestoreCommandParser parser = new RestoreCommandParser();

    @Test
    public void parse_validName_returnsCommand() throws Exception {
        assertEquals(new RestoreCommand("before-clearing.json"), parser.parse(" f/before-clearing.json "));
        assertEquals(new RestoreCommand("before-clearing.json"),
                new AddressBookParser().parseCommand("restore f/before-clearing.json"));
    }

    @Test
    public void parse_invalidInput_rejects() {
        String[] bad = {"", "   ", "f/", "f/   ", "x/a.json", "f/a.json f/b.json", "extra f/a.json"};
        for (String args : bad) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }
}
