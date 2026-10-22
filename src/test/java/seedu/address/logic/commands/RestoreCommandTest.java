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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class RestoreCommandTest {

    @TempDir
    Path tempDir;

    private Model newModel() {
        return new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    /** Writes {@code book} into the backup folder under {@code name}. */
    private Path writeBackup(AddressBook book, String name) throws Exception {
        Path dir = tempDir.resolve("backup");
        Files.createDirectories(dir);
        Path target = dir.resolve(name);
        new JsonAddressBookStorage(target).saveAddressBook(book);
        return dir;
    }

    @Test
    public void execute_validBackup_replacesDataAsOneUndoableStepAndClearsFilters() throws Exception {
        Model model = newModel();
        AddressBook backupBook = new AddressBook();
        backupBook.setPersons(List.of(new PersonBuilder().withName("Restored Client").build()));
        Path dir = writeBackup(backupBook, "saved.json");
        model.updateFilteredPersonList(person -> person.getName().fullName.startsWith("Alice"));
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());

        CommandResult result = new RestoreCommand("saved.json", dir).execute(model);

        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals("Restored Client", model.getAddressBook().getPersonList().get(0).getName().fullName);
        assertEquals("Restored 1 clients from saved.json", result.getFeedbackToUser());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList(),
                "filters are cleared so the restored list is visible");
        assertTrue(model.canUndoAddressBook());

        model.undoAddressBook();
        assertEquals(before, model.getAddressBook().getPersonList(), "one undo brings the old data back");
    }

    @Test
    public void execute_missingFile_reportsTheNameAndChangesNothing() throws Exception {
        Model model = newModel();
        Path dir = tempDir.resolve("backup");
        Files.createDirectories(dir);
        boolean canUndoBefore = model.canUndoAddressBook();

        CommandException thrown = assertThrows(CommandException.class, () ->
                new RestoreCommand("absent.json", dir).execute(model));

        assertEquals("No backup file named absent.json in the data folder.", thrown.getMessage());
        assertEquals(canUndoBefore, model.canUndoAddressBook());
    }

    @Test
    public void execute_notAClientList_leavesDataFiltersAndRedoAlone() throws Exception {
        Model model = newModel();
        Path dir = tempDir.resolve("backup");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("broken.json"), "{\"unrelated\": true}");
        model.addPerson(new PersonBuilder().withName("Zed Restore").build());
        model.undoAddressBook();
        assertTrue(model.canRedoAddressBook());
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());

        CommandException thrown = assertThrows(CommandException.class, () ->
                new RestoreCommand("broken.json", dir).execute(model));

        assertEquals("That file is not a valid client list, so nothing was restored.", thrown.getMessage());
        assertEquals(before, model.getAddressBook().getPersonList());
        assertTrue(model.canRedoAddressBook(), "a rejected restore must not discard redo");
    }

    @Test
    public void execute_emptyFile_reportsNotAClientList() throws Exception {
        Model model = newModel();
        Path dir = tempDir.resolve("backup");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("empty.json"), "");

        CommandException thrown = assertThrows(CommandException.class, () ->
                new RestoreCommand("empty.json", dir).execute(model));
        assertEquals("That file is not a valid client list, so nothing was restored.", thrown.getMessage());
    }

    @Test
    public void execute_folderSeparator_isTreatedAsMissing() {
        Model model = newModel();
        CommandException thrown = assertThrows(CommandException.class, () ->
                new RestoreCommand("../addressbook.json", tempDir).execute(model));
        assertEquals("No backup file named ../addressbook.json in the data folder.", thrown.getMessage());
    }

    @Test
    public void execute_unusableFileName_isTreatedAsMissing() throws Exception {
        Path dir = writeBackup(new AddressBook(), "placeholder.json");
        RestoreCommand command = new RestoreCommand("bad?.json", dir);
        CommandException e = assertThrows(CommandException.class, () -> command.execute(newModel()));
        assertEquals(String.format(RestoreCommand.MESSAGE_MISSING, "bad?.json"), e.getMessage());
    }

    @Test
    public void execute_nullClientList_reportsNotAClientList() throws Exception {
        Path dir = writeBackup(new AddressBook(), "placeholder.json");
        Files.writeString(dir.resolve("nulls.json"), "{\"persons\": null}");
        RestoreCommand command = new RestoreCommand("nulls.json", dir);
        CommandException e = assertThrows(CommandException.class, () -> command.execute(newModel()));
        assertEquals(RestoreCommand.MESSAGE_NOT_A_LIST, e.getMessage());
    }

    @Test
    public void equals() {
        RestoreCommand first = new RestoreCommand("a.json", tempDir);
        assertEquals(first, new RestoreCommand("a.json", tempDir));
        assertNotEquals(first, new RestoreCommand("b.json", tempDir));
        assertFalse(first.equals(null));
    }
}
