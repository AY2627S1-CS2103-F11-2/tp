package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class AppointmentIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_filteredDelete_savesCorrectAppointmentAndRestartShowsRemaining() throws Exception {
        Path file = directory.resolve("schedule.json");
        JsonAddressBookStorage disk = new JsonAddressBookStorage(file);
        ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        LogicManager logic = logic(model, disk);
        disk.saveAddressBook(model.getAddressBook());
        model.updateFilteredPersonList(p -> p.equals(BOB));
        logic.execute("appointments c/C1");
        assertEquals(List.of(SIGNING), logic.getFilteredAppointmentList());
        logic.execute("deleteappt A1 confirm");
        assertEquals(2, logic.getFilteredAppointmentList().size());
        assertEquals(List.of(BOB), logic.getFilteredPersonList());
        ModelManager restarted = new ModelManager(disk.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), restarted.getAddressBook());
        assertEquals(2, restarted.getFilteredAppointmentList().size());
        assertEquals(2, restarted.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidInput_preservesFileAndFilters() throws Exception {
        Path file = directory.resolve("schedule.json");
        JsonAddressBookStorage disk = new JsonAddressBookStorage(file);
        ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        LogicManager logic = logic(model, disk);
        disk.saveAddressBook(model.getAddressBook());
        logic.execute("appointments c/C2");
        String before = Files.readString(file);
        for (String command : new String[]{"deleteappt A1", "deleteappt A1 no", "deleteappt A1 yes extra",
            "appointments d/30022026", "appointments c/C1 c/C2",
            "appointment n/Clash c/C1 loc/NUS s/20261015-1000 e/20261015-1100"}) {
            Class<? extends Exception> expected = command.startsWith("appointment n/")
                    ? CommandException.class : ParseException.class;
            assertThrows(expected, () -> logic.execute(command), command);
            assertEquals(before, Files.readString(file));
            assertEquals(List.of(SIGNING), logic.getFilteredAppointmentList());
        }
        assertThrows(CommandException.class, () -> logic.execute("deleteappt A2 yes"));
        assertThrows(CommandException.class, () -> logic.execute("appointments c/C3"));
        assertThrows(ParseException.class, () -> logic.execute("deleteappt A0 yes"));
        assertEquals(before, Files.readString(file));
        assertEquals(List.of(SIGNING), logic.getFilteredAppointmentList());
    }

    @Test
    public void execute_creationClientEditDeletionAndClear_persistConsistently() throws Exception {
        Path file = directory.resolve("schedule.json");
        JsonAddressBookStorage disk = new JsonAddressBookStorage(file);
        AddressBook book = new AddressBook();
        book.addPerson(BOB);
        ModelManager model = new ModelManager(book, new UserPrefs());
        LogicManager logic = logic(model, disk);
        logic.execute("appointment n/Viewing c/C1 loc/NUS s/20261015-2330 e/20261016-0030");
        logic.execute("appointments d/15102026");
        assertEquals(1, logic.getFilteredAppointmentList().size());
        logic.execute("appointments d/16102026");
        assertTrue(logic.getFilteredAppointmentList().isEmpty());
        logic.execute("edit 1 n/Bob Updated");
        assertEquals("Bob Updated", disk.readAddressBook().orElseThrow().getAppointmentList()
                .get(0).getClient().getName().fullName);
        logic.execute("delete C1");
        assertTrue(disk.readAddressBook().orElseThrow().getAppointmentList().isEmpty());
        model.setAddressBook(getAppointmentAddressBook());
        logic.execute("clear");
        assertTrue(disk.readAddressBook().orElseThrow().getAppointmentList().isEmpty());
        assertTrue(disk.readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void execute_viewingDoesNotWriteEvenWhenStorageCannotSave() throws Exception {
        ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        JsonAddressBookStorage failingDisk = new JsonAddressBookStorage(directory.resolve("schedule.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Simulated full disk");
            }
        };
        LogicManager logic = logic(model, failingDisk);
        assertEquals("Showing 3 appointments.", logic.execute("appointments").getFeedbackToUser());
        assertThrows(CommandException.class, () -> logic.execute("deleteappt A1 yes"));
    }

    private LogicManager logic(ModelManager model, JsonAddressBookStorage disk) {
        return new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }
}
