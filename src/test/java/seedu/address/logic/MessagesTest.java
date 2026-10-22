package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class MessagesTest {

    private static final Path DATA_FILE = Paths.get("data", "addressbook.json");
    private static final Path RENAMED = Paths.get("data", "addressbook.json.corrupt-2026-10-20-2130");

    @Test
    public void formatUnreadableDataFile_renamed_namesTheDataFileAndTheKeptCopy() {
        String message = Messages.formatUnreadableDataFile(DATA_FILE, Optional.of(RENAMED));

        assertTrue(message.contains(DATA_FILE.toString()), message);
        assertTrue(message.contains(RENAMED.toString()), message);
        assertTrue(message.contains("empty list"), message);
        assertFalse(message.contains("could not be renamed"), message);
    }

    @Test
    public void formatUnreadableDataFile_renameFailed_saysTheFileIsStillThere() {
        String message = Messages.formatUnreadableDataFile(DATA_FILE, Optional.empty());

        assertTrue(message.contains(DATA_FILE.toString()), message);
        assertTrue(message.contains("could not be renamed"), message);
        assertFalse(message.contains("was renamed to"), message);
    }
}
