package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Drives the backup command through the real parser and the real save path. */
public class BackupIntegrationTest {

    private static final String NAME = "it-test-backup.json";

    @TempDir
    public Path directory;

    @AfterEach
    public void deleteBackup() throws Exception {
        Files.deleteIfExists(BackupCommand.DEFAULT_BACKUP_DIR.resolve(NAME));
    }

    @Test
    public void execute_throughTheParser_writesTheFileAndChangesNothingElse() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path dataFile = directory.resolve("addressbook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        LogicManager logic = new LogicManager(model, storage);

        logic.execute("add n/Zed Backup p/91234567 e/zed@example.com a/Blk 9 Tampines y/buyer");
        logic.execute("undo");
        assertTrue(model.canRedoAddressBook());
        logic.execute("search n/Alice");
        List<Person> shownBefore = List.copyOf(model.getFilteredPersonList());
        Files.writeString(dataFile, "sentinel");

        CommandResult result = logic.execute("backup f/" + NAME);

        Path target = BackupCommand.DEFAULT_BACKUP_DIR.resolve(NAME);
        assertTrue(Files.exists(target), target.toString());
        assertEquals(getTypicalAddressBook().getPersonList(),
                new JsonAddressBookStorage(target).readAddressBook().orElseThrow().getPersonList());
        assertEquals("sentinel", Files.readString(dataFile), "the automatic save should be skipped");
        assertTrue(model.canRedoAddressBook(), "redo must survive a backup");
        assertEquals(shownBefore, model.getFilteredPersonList(), "filters must survive a backup");
        assertTrue(result.getFeedbackToUser().startsWith("Backed up "), result.getFeedbackToUser());
    }
}
