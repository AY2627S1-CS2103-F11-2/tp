package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.ClientId;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class AppointmentHistoryIntegrationTest {
    private static final String CREATE =
            "appointment n/Viewing c/C1 loc/NUS COM3 #02-01 s/20261017-1000 e/20261017-1100";

    @TempDir
    public Path directory;

    @Test
    public void undo_creationAfterClientEdit_reversesOneChangeAtATimeAndPersists() throws Exception {
        AddressBook initial = oneClientBook();
        Fixture fixture = fixture(initial);
        fixture.logic.execute("edit 1 n/Alice Updated");
        AddressBook afterEdit = new AddressBook(fixture.model.getAddressBook());
        fixture.logic.execute(CREATE);
        AddressBook afterCreation = new AddressBook(fixture.model.getAddressBook());

        fixture.logic.execute("undo");
        assertState(fixture, afterEdit);
        fixture.logic.execute("undo");
        assertState(fixture, initial);
        fixture.logic.execute("redo");
        assertState(fixture, afterEdit);
        fixture.logic.execute("redo");
        assertState(fixture, afterCreation);
    }

    @Test
    public void create_afterUndo_discardsRedoAndKeepsNewAppointmentOnDisk() throws Exception {
        Fixture fixture = fixture(oneClientBook());
        fixture.logic.execute("edit 1 n/Alice Updated");
        fixture.logic.execute("undo");
        fixture.logic.execute(CREATE);
        AddressBook afterCreation = new AddressBook(fixture.model.getAddressBook());

        assertThrows(CommandException.class, () -> fixture.logic.execute("redo"));
        assertState(fixture, afterCreation);
        fixture.logic.execute("undo");
        assertState(fixture, oneClientBook());
    }

    @Test
    public void undoAndRedo_clientEdit_resetBothFiltersAndKeepAppointmentLinks() throws Exception {
        Fixture fixture = fixture(getAppointmentAddressBook());
        fixture.logic.execute("edit 1 n/Alice Updated");
        AddressBook afterEdit = new AddressBook(fixture.model.getAddressBook());
        fixture.logic.execute("search n/Alice");
        fixture.logic.execute("appointments c/C1 d/15102026");
        assertEquals(1, fixture.model.getFilteredAppointmentList().size());

        fixture.logic.execute("undo");
        assertState(fixture, getAppointmentAddressBook());
        assertEquals(2, fixture.model.getFilteredPersonList().size());
        assertEquals(3, fixture.model.getFilteredAppointmentList().size());
        fixture.logic.execute("search n/Alice");
        fixture.logic.execute("appointments c/C1 d/16102026");
        fixture.logic.execute("redo");
        assertState(fixture, afterEdit);
        assertEquals(2, fixture.model.getFilteredPersonList().size());
        assertEquals(3, fixture.model.getFilteredAppointmentList().size());
    }

    @Test
    public void confirmedDelete_clearsBothHistoriesAndCannotResurrectAppointment() throws Exception {
        Fixture fixture = fixture(getAppointmentAddressBook());
        fixture.logic.execute("edit 1 n/Alice Updated");
        fixture.logic.execute("edit 1 p/81234567");
        fixture.logic.execute("undo");
        assertTrue(fixture.model.canUndoAddressBook());
        assertTrue(fixture.model.canRedoAddressBook());
        fixture.logic.execute("appointments d/15102026");
        fixture.logic.execute("deleteappt A1 yes");
        AddressBook baseline = new AddressBook(fixture.model.getAddressBook());

        assertFalse(fixture.model.canUndoAddressBook());
        assertFalse(fixture.model.canRedoAddressBook());
        assertThrows(CommandException.class, () -> fixture.logic.execute("undo"));
        assertThrows(CommandException.class, () -> fixture.logic.execute("redo"));
        assertState(fixture, baseline);
        fixture.logic.execute("edit 1 p/82345678");
        fixture.logic.execute("undo");
        assertState(fixture, baseline);
        assertThrows(CommandException.class, () -> fixture.logic.execute("undo"));
        assertEquals(2, fixture.disk.readAddressBook().orElseThrow().getAppointmentList().size());
    }

    @Test
    public void deletingClientAndClearingData_undoRestoresLinkedAppointmentsTogether() throws Exception {
        Fixture fixture = fixture(getAppointmentAddressBook());
        fixture.logic.execute("delete C1");
        assertEquals(1, fixture.model.getAddressBook().getAppointmentList().size());
        fixture.logic.execute("undo");
        assertState(fixture, getAppointmentAddressBook());
        fixture.logic.execute("clear");
        AddressBook cleared = new AddressBook();
        cleared.setNextClientId(3);
        assertState(fixture, cleared);
        assertTrue(fixture.model.getAddressBook().getPersonList().isEmpty());
        assertTrue(fixture.model.getAddressBook().getAppointmentList().isEmpty());
        AddressBook restartedCleared = new AddressBook(fixture.disk.readAddressBook().orElseThrow());
        restartedCleared.addPerson(ALICE);
        assertEquals(new ClientId(3), restartedCleared.getPersonList().get(0).getClientId().orElseThrow());
        fixture.logic.execute("undo");
        assertState(fixture, getAppointmentAddressBook());
    }

    @Test
    public void restart_keepsAppointmentsAndStartsWithEmptyHistory() throws Exception {
        Fixture fixture = fixture(oneClientBook());
        fixture.logic.execute(CREATE);
        ModelManager restarted = new ModelManager(fixture.disk.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(fixture.model.getAddressBook(), restarted.getAddressBook());
        assertFalse(restarted.canUndoAddressBook());
        assertFalse(restarted.canRedoAddressBook());
    }

    @Test
    public void appointmentReplacement_savedDataAndUndoRedoRoundTripAllFields() throws Exception {
        Fixture fixture = fixture(getAppointmentAddressBook());
        Appointment replacement = new Appointment("Inspection", BOB, "NUS COM3 #02-01",
                VIEWING.getStart().plusDays(4), VIEWING.getEnd().plusDays(4), "Updated remarks");
        // Persist this direct model-API change explicitly; read-only list must not save it.
        fixture.model.setAppointment(VIEWING, replacement);
        fixture.disk.saveAddressBook(fixture.model.getAddressBook());
        AddressBook edited = new AddressBook(fixture.model.getAddressBook());
        assertState(fixture, edited);
        fixture.logic.execute("undo");
        assertState(fixture, getAppointmentAddressBook());
        fixture.logic.execute("redo");
        assertState(fixture, edited);
        ModelManager restarted = new ModelManager(fixture.disk.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(restarted.getFilteredAppointmentList().contains(replacement));
    }

    @Test
    public void readOnlyNoOpAndRejectedCommands_afterUndo_preserveRedo() throws Exception {
        Fixture fixture = fixture(getAppointmentAddressBook());
        fixture.logic.execute("edit 1 n/Alice Updated");
        AddressBook edited = new AddressBook(fixture.model.getAddressBook());
        fixture.logic.execute("undo");
        fixture.logic.execute("list");
        fixture.logic.execute("search n/Alice");
        fixture.logic.execute("help");
        fixture.logic.execute("appointments c/C1 d/15102026");
        fixture.logic.execute("edit 1 n/" + ALICE.getName().fullName);
        fixture.logic.execute("search n/Alice");
        assertThrows(ParseException.class, () -> fixture.logic.execute("deleteappt A1"));
        assertThrows(CommandException.class, () -> fixture.logic.execute("deleteappt A999 yes"));
        assertThrows(CommandException.class, () -> fixture.logic.execute(
                "appointment n/Clash c/C1 loc/NUS s/20261015-1000 e/20261015-1100"));
        assertState(fixture, getAppointmentAddressBook());
        assertEquals(List.of(ALICE), fixture.model.getFilteredPersonList());
        assertEquals(List.of(VIEWING), fixture.model.getFilteredAppointmentList());
        fixture.logic.execute("redo");
        assertState(fixture, edited);
    }

    @Test
    public void unavailableUndoAndRedo_preserveFiltersAndData() throws Exception {
        Fixture fixture = fixture(getAppointmentAddressBook());
        fixture.logic.execute("search n/Alice");
        fixture.logic.execute("appointments c/C1 d/15102026");
        assertThrows(CommandException.class, () -> fixture.logic.execute("undo"));
        assertThrows(CommandException.class, () -> fixture.logic.execute("redo"));
        assertState(fixture, getAppointmentAddressBook());
        assertEquals(List.of(ALICE), fixture.model.getFilteredPersonList());
        assertEquals(List.of(VIEWING), fixture.model.getFilteredAppointmentList());
    }

    private AddressBook oneClientBook() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        return book;
    }

    private Fixture fixture(AddressBook initial) throws IOException {
        ModelManager model = new ModelManager(initial, new UserPrefs());
        JsonAddressBookStorage disk = new JsonAddressBookStorage(directory.resolve("schedule.json"));
        disk.saveAddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
        return new Fixture(model, logic, disk);
    }

    private void assertState(Fixture fixture, AddressBook expected) throws Exception {
        assertEquals(expected, fixture.model.getAddressBook());
        assertEquals(expected, new AddressBook(fixture.disk.readAddressBook().orElseThrow()));
    }

    private record Fixture(ModelManager model, LogicManager logic, JsonAddressBookStorage disk) {}
}
