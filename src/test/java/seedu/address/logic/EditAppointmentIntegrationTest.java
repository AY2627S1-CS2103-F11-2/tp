package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditAppointmentCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class EditAppointmentIntegrationTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private JsonAddressBookStorage disk;
    private LogicManager logic;
    private Path file;

    @BeforeEach
    public void setUp() throws Exception {
        file = directory.resolve("schedule.json");
        disk = new JsonAddressBookStorage(file);
        model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        logic = new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
        disk.saveAddressBook(model.getAddressBook());
    }

    @Test
    public void execute_filteredEdit_preservesOtherFieldsFiltersAndSavedData() throws Exception {
        model.updateFilteredPersonList(BOB::equals);
        logic.execute("appointments c/C1 d/15102026");
        Appointment expected = withLocation(SIGNING, "NUS  COM3 #02-01");
        assertEquals("Updated appointment A1 (Signing) location: NUS  COM3 #02-01",
                logic.execute("editappt A1 loc/  NUS  COM3 #02-01  ").getFeedbackToUser());
        assertEquals(List.of(expected), model.getFilteredAppointmentList());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        AddressBook expectedBook = getAppointmentAddressBook();
        expectedBook.setAppointment(SIGNING, expected);
        assertEquals(expectedBook, model.getAddressBook());
        assertEquals(expectedBook, disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_unfilteredEdit_usesChronologicalIndexAndSupportsUndoRedo() throws Exception {
        logic.execute("editappt A1 loc/New venue");
        assertEquals(withLocation(VIEWING, "New venue"), model.getFilteredAppointmentList().get(0));
        logic.execute("undo");
        assertEquals(getAppointmentAddressBook(), disk.readAddressBook().orElseThrow());
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertEquals(withLocation(VIEWING, "New venue"), model.getFilteredAppointmentList().get(0));
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_invalidOrUnchangedInput_preservesDataFiltersAndRedo() throws Exception {
        logic.execute("editappt A1 loc/New venue");
        logic.execute("undo");
        logic.execute("appointments c/C2");
        String before = Files.readString(file);
        for (String command : new String[]{"editappt A1 loc/", "editappt A1 loc/NUS loc/COM3",
            "editappt A0 loc/NUS", "editappt A1 loc/NUS n/Other"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
        }
        assertThrows(CommandException.class, () -> logic.execute("editappt A2 loc/New venue"));
        CommandException unchanged = assertThrows(CommandException.class, ()
            -> logic.execute("editappt A1 loc/ NUS "));
        assertEquals(EditAppointmentCommand.MESSAGE_UNCHANGED, unchanged.getMessage());
        assertEquals(before, Files.readString(file));
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        assertTrue(model.canRedoAddressBook());
        logic.execute("appointments d/01012000");
        assertThrows(CommandException.class, () -> logic.execute("editappt A1 loc/New venue"));
        assertTrue(model.getFilteredAppointmentList().isEmpty());
        assertEquals(before, Files.readString(file));
    }

    private Appointment withLocation(Appointment target, String location) {
        return new Appointment(target.getName(), target.getClient(), location,
                target.getStart(), target.getEnd(), target.getRemarks());
    }
}
