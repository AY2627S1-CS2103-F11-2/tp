package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
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
        assertEquals("Edited Appointment: [Index 1] Title: Signing; Client: " + BOB.getName().fullName
                + "; Date & Time: 2026-10-15 14:00; Location: NUS  COM3 #02-01; Description: ",
                logic.execute("editappt 1 loc/  NUS  COM3 #02-01  ").getFeedbackToUser());
        assertEquals(List.of(expected), model.getFilteredAppointmentList());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        AddressBook expectedBook = getAppointmentAddressBook();
        expectedBook.setAppointment(SIGNING, expected);
        assertEquals(expectedBook, model.getAddressBook());
        assertEquals(expectedBook, disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_unfilteredEdit_usesChronologicalIndexAndSupportsUndoRedo() throws Exception {
        logic.execute("editappt 1 loc/New venue");
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
        logic.execute("editappt 1 loc/New venue");
        logic.execute("undo");
        logic.execute("appointments c/C2");
        String before = Files.readString(file);
        for (String command : new String[]{"editappt 1 loc/", "editappt 1 loc/NUS loc/COM3",
            "editappt 0 loc/NUS", "editappt 1 loc/NUS x/Other"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
        }
        assertThrows(CommandException.class, () -> logic.execute("editappt 2 loc/New venue"));
        assertEquals(String.format(EditAppointmentCommand.MESSAGE_UNCHANGED, 1),
                logic.execute("editappt 1 loc/ NUS ").getFeedbackToUser());
        assertEquals(before, Files.readString(file));
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        assertTrue(model.canRedoAddressBook());
        logic.execute("appointments d/01012000");
        assertThrows(CommandException.class, () -> logic.execute("editappt 1 loc/New venue"));
        assertTrue(model.getFilteredAppointmentList().isEmpty());
        assertEquals(before, Files.readString(file));
    }

    @Test
    public void execute_createThenEdit_separateUndoRedoStepsAndPersistence() throws Exception {
        logic.execute("appointment n/Meeting c/C2 loc/NUS s/20261017-1000 e/20261017-1100");
        AddressBook created = new AddressBook(model.getAddressBook());
        logic.execute("editappt 4 loc/COM3");
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
        logic.execute("editappt 1 loc/First venue");
        logic.execute("undo");
        assertTrue(model.canRedoAddressBook());
        logic.execute("editappt 1 loc/Second venue");
        assertFalse(model.canRedoAddressBook());
        assertThrows(CommandException.class, () -> logic.execute("redo"));
        assertEquals(withLocation(VIEWING, "Second venue"), model.getFilteredAppointmentList().get(0));
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_editAfterConfirmedDeletion_undoDoesNotResurrectDeletedAppointment() throws Exception {
        logic.execute("deleteappt A1 yes");
        AddressBook baseline = new AddressBook(model.getAddressBook());
        logic.execute("editappt 1 loc/New venue");
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
        logic.execute("editappt 1 loc/Future venue");
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
        CommandResult unchanged = failingLogic.execute("editappt 1 n/Signing c/1 d/2026-10-15 14:00 loc/NUS desc/");
        assertFalse(unchanged.isSaveRequired());
        assertEquals(before, Files.readString(file));
        assertTrue(model.canRedoAddressBook());
        assertThrows(CommandException.class, () -> failingLogic.execute("editappt 1 n/Failed 2 c/1 "
                + "d/2026-11-01 10:00 loc/Failed venue desc/Failed notes"));
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
        logic.execute("editappt 1 loc/Retry venue");
        assertEquals(List.of(withLocation(SIGNING, "Retry venue")), model.getFilteredAppointmentList());
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_allFields_preservesDurationAndRoundTripsHistoryAndDisk() throws Exception {
        Appointment longer = new Appointment(VIEWING.getName(), ALICE, VIEWING.getLocation(),
                VIEWING.getStart(), VIEWING.getEnd().plusMinutes(75), VIEWING.getRemarks());
        AddressBook baseline = getAppointmentAddressBook();
        baseline.setAppointment(VIEWING, longer);
        model = new ModelManager(baseline, new UserPrefs());
        logic = new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
        disk.saveAddressBook(model.getAddressBook());
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(ALICE, VIEWING.getStart().toLocalDate());

        logic.execute("editappt 1 n/Viewing 2 c/1 d/2026-11-01 10:00 loc/NUS COM3 #02-01 desc/Bring documents");

        Appointment expected = new Appointment("Viewing 2", BOB, "NUS COM3 #02-01",
                LocalDateTime.of(2026, 11, 1, 10, 0), LocalDateTime.of(2026, 11, 1, 12, 15), "Bring documents");
        assertTrue(model.getAddressBook().getAppointmentList().contains(expected));
        assertEquals(Duration.between(longer.getStart(), longer.getEnd()),
                Duration.between(expected.getStart(), expected.getEnd()));
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertTrue(model.getFilteredAppointmentList().isEmpty());
        AddressBook edited = new AddressBook(model.getAddressBook());
        assertEquals(edited, disk.readAddressBook().orElseThrow());
        assertEquals(edited, new ModelManager(disk.readAddressBook().orElseThrow(), new UserPrefs()).getAddressBook());
        logic.execute("undo");
        assertEquals(baseline, disk.readAddressBook().orElseThrow());
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertEquals(edited, disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_individualFields_preservesOmittedFieldsAndClearsDescription() throws Exception {
        logic.execute("editappt 1 n/Inspection 123");
        Appointment renamed = model.getFilteredAppointmentList().getFirst();
        assertEquals("Inspection 123", renamed.getName());
        assertEquals(VIEWING.getLocation(), renamed.getLocation());
        assertEquals(VIEWING.getStart(), renamed.getStart());
        assertEquals(VIEWING.getEnd(), renamed.getEnd());
        assertEquals(VIEWING.getRemarks(), renamed.getRemarks());
        logic.execute("editappt 1 c/2");
        assertEquals(BOB, model.getFilteredAppointmentList().getFirst().getClient());
        logic.execute("editappt 1 desc/");
        assertEquals("", model.getFilteredAppointmentList().getFirst().getRemarks());
        assertEquals(renamed.getName(), model.getFilteredAppointmentList().getFirst().getName());
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_timeClashWithHiddenAppointment_rejectsEveryFieldAndPreservesRedo() throws Exception {
        logic.execute("editappt 1 loc/Future venue");
        logic.execute("undo");
        model.updateFilteredAppointmentList(ALICE, VIEWING.getStart().toLocalDate());
        String before = Files.readString(file);
        CommandException error = assertThrows(CommandException.class, ()
                -> logic.execute("editappt 1 n/Changed 2 d/2026-10-15 14:30 loc/New venue desc/New notes"));
        assertEquals("Schedule clash with appointment: Signing", error.getMessage());
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(List.of(VIEWING), model.getFilteredAppointmentList());
        assertEquals(before, Files.readString(file));
        assertTrue(model.canRedoAddressBook());
    }

    @Test
    public void execute_shiftedTime_allowsAdjacentAppointmentsAndRejectsAnOverflowingEnd() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("editappt 1 d/9999-12-31 23:30"));
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
        logic.execute("editappt 1 d/2026-10-15 15:00");
        assertEquals(SIGNING, model.getFilteredAppointmentList().getFirst());
        Appointment moved = model.getFilteredAppointmentList().get(1);
        assertEquals(VIEWING.getName(), moved.getName());
        assertEquals(LocalDateTime.of(2026, 10, 15, 16, 0), moved.getEnd());
    }

    @Test
    public void execute_clientReference_usesDisplayedPositionRatherThanFullListOrStableId() throws Exception {
        model.updateFilteredPersonList(BOB::equals);
        String before = Files.readString(file);
        CommandException error = assertThrows(CommandException.class, () -> logic.execute("editappt 1 c/2"));
        assertEquals("The client index provided is invalid. (Must be a positive integer between 1 and 1)",
                error.getMessage());
        assertEquals(before, Files.readString(file));
        assertFalse(model.canUndoAddressBook());
        logic.execute("editappt 1 c/1");
        assertEquals(BOB, model.getFilteredAppointmentList().getFirst().getClient());
    }

    @Test
    public void execute_legacyLongName_survivesUnrelatedEditsAndReload() throws Exception {
        Appointment legacy = new Appointment("A".repeat(80), ALICE, VIEWING.getLocation(),
                VIEWING.getStart(), VIEWING.getEnd(), VIEWING.getRemarks());
        model.setAppointment(VIEWING, legacy);
        logic.execute("editappt 1 desc/Updated notes");
        assertEquals(legacy.getName(), model.getFilteredAppointmentList().getFirst().getName());
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    private Appointment withLocation(Appointment target, String location) {
        return new Appointment(target.getName(), target.getClient(), location,
                target.getStart(), target.getEnd(), target.getRemarks());
    }
}
