package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalAppointments.HANDOVER;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests documented client deletion, hidden linked appointments, persistence and shared history together. */
public class DeleteClientIntegrationTest {
    @TempDir
    public Path directory;

    private Path file;
    private JsonAddressBookStorage disk;
    private ModelManager model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        file = directory.resolve("addressbook.json");
        disk = new JsonAddressBookStorage(file);
        model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        logic = new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
        disk.saveAddressBook(model.getAddressBook());
    }

    @Test
    public void execute_deleteClient_removesHiddenAppointmentsAndRestoresThemWithUndoRedo() throws Exception {
        model.updateFilteredPersonList(ALICE::equals);
        logic.execute("appointments c/C1 d/15102026");
        assertEquals(List.of(VIEWING), model.getFilteredAppointmentList());

        String feedback = logic.execute("delete C1").getFeedbackToUser();

        assertEquals("Deleted Client: " + Messages.format(ALICE), feedback);
        assertEquals(List.of(BOB), model.getAddressBook().getPersonList());
        assertEquals(List.of(SIGNING), model.getAddressBook().getAppointmentList());
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        assertEquals(List.of(), model.getFilteredPersonList());
        AddressBook deleted = new AddressBook(model.getAddressBook());
        assertEquals(deleted, disk.readAddressBook().orElseThrow());

        logic.execute("undo");
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(getAppointmentAddressBook(), disk.readAddressBook().orElseThrow());
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
        assertEquals(List.of(VIEWING, SIGNING, HANDOVER), model.getFilteredAppointmentList());

        logic.execute("redo");
        assertEquals(deleted, model.getAddressBook());
        assertEquals(deleted, disk.readAddressBook().orElseThrow());
        ModelManager restarted = new ModelManager(disk.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(deleted, restarted.getAddressBook());
        assertFalse(restarted.canUndoAddressBook());
    }

    @Test
    public void execute_filteredClient_removesHiddenAppointmentAndKeepsOtherClientsAppointments() throws Exception {
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(ALICE, null);

        String feedback = logic.execute("delete C1").getFeedbackToUser();

        assertEquals("Deleted Client: " + Messages.format(BOB), feedback);
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(HANDOVER, VIEWING), model.getAddressBook().getAppointmentList());
        assertEquals(List.of(VIEWING, HANDOVER), model.getFilteredAppointmentList());
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_invalidReference_preservesDataFiltersFileAndRedo() throws Exception {
        logic.execute("delete C1");
        logic.execute("undo");
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(ALICE, VIEWING.getStart().toLocalDate());
        String savedData = Files.readString(file);

        assertThrows(CommandException.class, () -> logic.execute("delete C2"));

        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(List.of(VIEWING), model.getFilteredAppointmentList());
        assertEquals(savedData, Files.readString(file));
        logic.execute("redo");
        assertEquals(List.of(BOB), model.getAddressBook().getPersonList());
        assertEquals(List.of(SIGNING), model.getAddressBook().getAppointmentList());
    }
}
