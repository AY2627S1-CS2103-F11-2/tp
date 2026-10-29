package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises read-only commands through the real parser, model and file storage. */
public class ReadOnlyCommandIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_listWithUnwritableDataFile_succeeds() throws Exception {
        assertSucceedsWithUnwritableDataFile("list");
    }

    @Test
    public void execute_searchWithUnwritableDataFile_succeeds() throws Exception {
        assertSucceedsWithUnwritableDataFile("search n/Alice");
    }

    @Test
    public void execute_helpWithUnwritableDataFile_succeeds() throws Exception {
        assertSucceedsWithUnwritableDataFile("help");
    }

    @Test
    public void execute_exitWithUnwritableDataFile_succeeds() throws Exception {
        assertSucceedsWithUnwritableDataFile("exit");
    }

    private void assertSucceedsWithUnwritableDataFile(String command) throws Exception {
        Model model = modelWithRedoAndFilters();
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Appointment> appointmentsBefore = List.copyOf(model.getFilteredAppointmentList());
        boolean canUndoBefore = model.canUndoAddressBook();
        Path dataFile = Files.createDirectory(directory.resolve("blocked.json"));

        CommandResult result = logicFor(model, dataFile).execute(command);

        assertFalse(result.isSaveRequired());
        assertEquals(command.equals("help"), result.isShowHelp());
        assertEquals(command.equals("exit"), result.isExit());
        assertEquals(before, model.getAddressBook());
        assertEquals(appointmentsBefore, model.getFilteredAppointmentList());
        assertEquals(canUndoBefore, model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        assertTrue(Files.isDirectory(dataFile));
        if (command.equals("list")) {
            assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        } else {
            assertEquals(List.of(before.getPersonList().getFirst()), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_readOnlyCommands_doNotRewriteExistingFile() throws Exception {
        Model model = modelWithRedoAndFilters();
        Path dataFile = directory.resolve("addressbook.json");
        Files.writeString(dataFile, "existing file must not be rewritten by a read-only command");

        Logic logic = logicFor(model, dataFile);
        for (String command : List.of("list", "search n/Alice", "help", "exit")) {
            logic.execute(command);
            assertEquals("existing file must not be rewritten by a read-only command", Files.readString(dataFile),
                    command);
            assertTrue(model.canRedoAddressBook(), command);
        }
    }

    private Model modelWithRedoAndFilters() {
        Model model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        Person alice = model.getAddressBook().getPersonList().getFirst();
        model.addPerson(new PersonBuilder().withName("Zed Read Only").build());
        model.undoAddressBook();
        model.updateFilteredPersonList(person -> person.getName().fullName.startsWith("Alice"));
        model.updateFilteredAppointmentList(alice, LocalDate.of(2026, 10, 15));
        return model;
    }

    private Logic logicFor(Model model, Path dataFile) {
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        return new LogicManager(model, storage);
    }
}
