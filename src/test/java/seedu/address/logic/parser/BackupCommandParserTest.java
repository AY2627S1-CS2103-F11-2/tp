package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class BackupCommandParserTest {

    private final BackupCommandParser parser = new BackupCommandParser();

    @Test
    public void parse_noArgument_returnsCommandUsingTheDefaultName() throws Exception {
        assertEquals(new BackupCommand(Optional.empty()), parser.parse(""));
        assertEquals(new BackupCommand(Optional.empty()), parser.parse("   "));
        assertEquals(new BackupCommand(Optional.empty()), new AddressBookParser().parseCommand("backup"));
    }

    @Test
    public void parse_validFileName_returnsCommand() throws Exception {
        assertEquals(new BackupCommand(Optional.of("before-clearing.json")),
                parser.parse(" f/before-clearing.json "));
        assertEquals(new BackupCommand(Optional.of("a b.json")), parser.parse(" f/  a b.json  "));
    }

    @Test
    public void parse_invalidInput_rejects() {
        String[] bad = {" f/", " f/   ", " f/x.txt", " f/dir/x.json", " f/../x.json", " f/a<b.json",
            " extra", " f/a.json f/b.json", " x/a.json", " f/a.json extra", " f/a.json\tf/b.json"};
        for (String args : bad) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }
}
