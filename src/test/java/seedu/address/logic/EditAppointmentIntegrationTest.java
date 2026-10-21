package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
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
import seedu.address.model.ReadOnlyAddressBook;
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

    @Test
    public void execute_createThenEdit_separateUndoRedoStepsAndPersistence() throws Exception {
        logic.execute("appointment n/Meeting c/C2 loc/NUS s/20261017-1000 e/20261017-1100");
        AddressBook created = new AddressBook(model.getAddressBook());
        logic.execute("editappt A4 loc/COM3");
        AddressBook edited = new AddressBook(model.getAddressBook());
        logic.execute("undo");
        assertEquals(created, disk.readAddressBook().orElseThrow());
        logic.execute("undo");
        assertEquals(getAppointmentAddressBook(), disk.readAddressBook().orElseThrow());
        logic.execute("redo");
        assertEquals(created, disk.readAddressBook().orElseThrow());
        logic.execute("redo");
        assertEquals(edited, disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_editAfterUndo_discardsRedoBranch() throws Exception {
        logic.execute("editappt A1 loc/First venue");
        logic.execute("undo");
        assertTrue(model.canRedoAddressBook());
        logic.execute("editappt A1 loc/Second venue");
        assertFalse(model.canRedoAddressBook());
        assertThrows(CommandException.class, () -> logic.execute("redo"));
        assertEquals(withLocation(VIEWING, "Second venue"), model.getFilteredAppointmentList().get(0));
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_editAfterConfirmedDeletion_undoDoesNotResurrectDeletedAppointment() throws Exception {
        logic.execute("deleteappt A1 yes");
        AddressBook baseline = new AddressBook(model.getAddressBook());
        logic.execute("editappt A1 loc/New venue");
        logic.execute("undo");
        assertEquals(baseline, disk.readAddressBook().orElseThrow());
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.getAddressBook().getAppointmentList().contains(VIEWING));
        logic.execute("redo");
        assertEquals(withLocation(SIGNING, "New venue"), model.getFilteredAppointmentList().get(0));
    }

    @Test
    public void execute_editSaveIoFailure_restoresDataFiltersAndRedo() throws Exception {
        assertFailedSaveRollback(new IOException("Disk unavailable"));
    }

    @Test
    public void execute_editSavePermissionFailure_restoresDataFiltersAndRedo() throws Exception {
        assertFailedSaveRollback(new AccessDeniedException(file.toString()));
    }

    private void assertFailedSaveRollback(IOException failure) throws Exception {
        logic.execute("editappt A1 loc/Future venue");
        AddressBook future = new AddressBook(model.getAddressBook());
        logic.execute("undo");
        model.updateFilteredPersonList(BOB::equals);
        logic.execute("appointments c/C1 d/15102026");
        String before = Files.readString(file);
        JsonAddressBookStorage failingDisk = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book, Path path) throws IOException {
                throw failure;
            }
        };
        LogicManager failingLogic = new LogicManager(model, new StorageManager(failingDisk,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
        assertThrows(CommandException.class, () -> failingLogic.execute("editappt A1 loc/Failed venue"));
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        assertEquals(before, Files.readString(file));
        assertTrue(model.canRedoAddressBook());
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertEquals(future, disk.readAddressBook().orElseThrow());
        logic.execute("undo");
        logic.execute("appointments c/C2");
        logic.execute("editappt A1 loc/Retry venue");
        assertEquals(List.of(withLocation(SIGNING, "Retry venue")), model.getFilteredAppointmentList());
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
        assertFalse(model.canRedoAddressBook());
    }

    private Appointment withLocation(Appointment target, String location) {
        return new Appointment(target.getName(), target.getClient(), location,
                target.getStart(), target.getEnd(), target.getRemarks());
    }
}
