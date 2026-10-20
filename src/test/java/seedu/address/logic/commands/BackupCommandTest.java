package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class BackupCommandTest {

    @TempDir
    Path tempDir;

    private Model newModel() {
        return new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_defaultName_writesTimestampedFileThatLoadsBack() throws Exception {
        Model model = newModel();
        Path backupDir = tempDir.resolve("backup");

        CommandResult result = new BackupCommand(Optional.empty(), backupDir).execute(model);

        List<Path> written;
        try (Stream<Path> files = Files.list(backupDir)) {
            written = files.toList();
        }
        assertEquals(1, written.size());
        String name = written.get(0).getFileName().toString();
        assertTrue(name.matches("clients-\\d{4}-\\d{2}-\\d{2}-\\d{6}\\.json"), name);

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(written.get(0)).readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook().getPersonList(), loaded.getPersonList());
        assertEquals(String.format("Backed up %d clients to %s.",
                model.getAddressBook().getPersonList().size(), written.get(0)), result.getFeedbackToUser());
    }

    @Test
    public void execute_explicitName_writesThatFile() throws Exception {
        Model model = newModel();
        Path backupDir = tempDir.resolve("backup");

        new BackupCommand(Optional.of("before-clearing.json"), backupDir).execute(model);

        assertTrue(Files.exists(backupDir.resolve("before-clearing.json")));
    }

    @Test
    public void execute_doesNotChangeDataOrFilters() throws Exception {
        Model model = newModel();
        model.updateFilteredPersonList(person -> person.getName().fullName.startsWith("Alice"));
        List<Person> shownBefore = List.copyOf(model.getFilteredPersonList());
        List<Person> storedBefore = List.copyOf(model.getAddressBook().getPersonList());

        new BackupCommand(Optional.empty(), tempDir.resolve("backup")).execute(model);

        assertEquals(shownBefore, model.getFilteredPersonList());
        assertEquals(storedBefore, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_historyAndFiltersAreUntouched() throws Exception {
        Model model = newModel();
        model.addPerson(new PersonBuilder().withName("Zed Backup").build());
        model.undoAddressBook();
        assertTrue(model.canRedoAddressBook());
        boolean canUndoBefore = model.canUndoAddressBook();
        List<Person> shownBefore = List.copyOf(model.getFilteredPersonList());

        new BackupCommand(Optional.empty(), tempDir.resolve("backup")).execute(model);

        assertTrue(model.canRedoAddressBook());
        assertEquals(canUndoBefore, model.canUndoAddressBook());
        assertEquals(shownBefore, model.getFilteredPersonList());
    }

    @Test
    public void execute_existingFile_isRejectedAndLeftAlone() throws Exception {
        Model model = newModel();
        Path backupDir = tempDir.resolve("backup");
        Files.createDirectories(backupDir);
        Path taken = backupDir.resolve("taken.json");
        Files.writeString(taken, "sentinel");

        assertThrows(CommandException.class, () ->
                new BackupCommand(Optional.of("taken.json"), backupDir).execute(model));
        assertEquals("sentinel", Files.readString(taken));
    }

    @Test
    public void execute_unwritableFolder_failsLoudly() throws Exception {
        Model model = newModel();
        Path blocked = tempDir.resolve("blocked");
        Files.writeString(blocked, "not a directory");

        CommandException thrown = assertThrows(CommandException.class, () ->
                new BackupCommand(Optional.of("x.json"), blocked).execute(model));
        assertTrue(thrown.getMessage().contains("Could not write the backup to"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Nothing was changed"), thrown.getMessage());
    }

    @Test
    public void equals() {
        Path dir = tempDir.resolve("backup");
        BackupCommand first = new BackupCommand(Optional.of("a.json"), dir);
        assertEquals(first, new BackupCommand(Optional.of("a.json"), dir));
        assertNotEquals(first, new BackupCommand(Optional.of("b.json"), dir));
        assertNotEquals(first, new BackupCommand(Optional.empty(), dir));
        assertFalse(first.equals(null));
    }
}
