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

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.RestoreCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Drives restore through the real parser and the real save path. */
public class RestoreIntegrationTest {

    private static final String NAME = "it-restore.json";

    @TempDir
    public Path directory;

    @AfterEach
    public void deleteBackup() throws Exception {
        Files.deleteIfExists(RestoreCommand.BACKUP_DIR.resolve(NAME));
    }

    @Test
    public void execute_throughTheParser_replacesDataSavesItAndCanBeUndone() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path dataFile = directory.resolve("addressbook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        LogicManager logic = new LogicManager(model, storage);

        AddressBook backup = new AddressBook();
        backup.addPerson(new PersonBuilder().withName("Restored Client").build());
        Files.createDirectories(RestoreCommand.BACKUP_DIR);
        new JsonAddressBookStorage(RestoreCommand.BACKUP_DIR.resolve(NAME)).saveAddressBook(backup);
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());

        CommandResult result = logic.execute("restore f/" + NAME);

        assertEquals("Restored 1 clients from " + NAME, result.getFeedbackToUser());
        assertEquals("Restored Client", model.getAddressBook().getPersonList().get(0).getName().fullName);
        assertEquals(1, new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow()
                .getPersonList().size(), "the restored data is saved to disk");

        logic.execute("undo");
        assertEquals(before, model.getAddressBook().getPersonList(), "undo returns to the pre-restore data");
        assertTrue(true);
    }
}
