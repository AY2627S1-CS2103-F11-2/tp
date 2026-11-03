package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks client terminology and normalized phone numbers through real commands and JSON storage. */
public class ClientFeedbackAndPhoneIntegrationTest {
    @TempDir
    public Path directory;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage dataStorage;

    @Test
    public void execute_editNamesConflictingClient_andUsesClientWording() throws Exception {
        initialize();
        logic.execute("add n/Alice Tan p/91234567 a/Clementi y/buyer");
        logic.execute("add n/Bob Lim p/92345678 a/Bedok y/seller");
        logic.execute("search n/Alice");
        String before = Files.readString(dataStorage.getAddressBookFilePath());

        CommandException duplicate = assertThrows(CommandException.class, () -> logic.execute("edit 1 n/BOB LIM"));

        assertEquals("Name already belongs to client C2 (Bob Lim). No client was edited.", duplicate.getMessage());
        assertEquals(before, Files.readString(dataStorage.getAddressBookFilePath()));
        assertEquals(1, model.getFilteredPersonList().size());
        CommandException invalidIndex = assertThrows(CommandException.class, () ->
                logic.execute("edit 99 n/Zed"));
        assertTrue(invalidIndex.getMessage().contains("client index"));
        assertTrue(logic.execute("edit 1 p/93456789").getFeedbackToUser().startsWith("Edited client: Alice Tan"));
    }

    @Test
    public void execute_clearReportsBothClientsAndAppointments() throws Exception {
        initialize();
        logic.execute("add n/Alice Tan p/91234567 a/Clementi y/buyer");
        logic.execute("appointment n/Viewing c/C1 loc/NUS s/20261105-1000 e/20261105-1100");

        String result = logic.execute("clear").getFeedbackToUser();

        assertEquals("All clients and appointments have been cleared.", result);
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.getAddressBook().getAppointmentList().isEmpty());
    }

    @Test
    public void execute_formattedPhones_normalizeAcrossAddEditSearchAndStorage() throws Exception {
        initialize();
        logic.execute("add n/Lim Ah Kow p/+65 9123 4567 a/Bedok y/buyer");
        var originalId = model.getFilteredPersonList().getFirst().getClientId();
        assertEquals("6591234567", model.getFilteredPersonList().getFirst().getPhone().value);
        logic.execute("search p/+65 9123 4567");
        assertEquals(1, model.getFilteredPersonList().size());
        logic.execute("search p/6591234567");
        assertEquals(1, model.getFilteredPersonList().size());

        logic.execute("edit 1 p/9234 5678");
        logic.execute("search p/92345678");

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(originalId, model.getFilteredPersonList().getFirst().getClientId());
        assertEquals("92345678", model.getFilteredPersonList().getFirst().getPhone().value);
        assertEquals(model.getAddressBook(), dataStorage.readAddressBook().orElseThrow());
        logic.execute("search p/+6591234567");
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_invalidPhones_leaveStoredDataSearchAndRedoUnchanged() throws Exception {
        initialize();
        logic.execute("add n/Alice Tan p/91234567 a/Clementi y/buyer");
        logic.execute("add n/Bob Lim p/92345678 a/Bedok y/seller");
        logic.execute("undo");
        logic.execute("search n/Alice");
        AddressBook before = new AddressBook(model.getAddressBook());
        String savedBefore = Files.readString(dataStorage.getAddressBookFilePath());

        for (String phone : List.of("1234567890123456789012345678901234567890", "000", "65+9123", "9123-4567")) {
            ParseException addFailure = assertThrows(ParseException.class, () ->
                    logic.execute("add n/Carol p/" + phone + " a/Bedok y/buyer"));
            assertEquals(Phone.MESSAGE_CONSTRAINTS, addFailure.getMessage());
            assertThrows(ParseException.class, () -> logic.execute("edit 1 p/" + phone));
            assertThrows(ParseException.class, () -> logic.execute("search p/" + phone));
            assertEquals(before, model.getAddressBook());
            assertEquals(savedBefore, Files.readString(dataStorage.getAddressBookFilePath()));
            assertEquals(1, model.getFilteredPersonList().size());
            assertTrue(model.canRedoAddressBook());
        }
        assertFalse(model.getFilteredPersonList().isEmpty());
    }

    private void initialize() throws Exception {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        dataStorage = new JsonAddressBookStorage(directory.resolve("addressbook.json"));
        dataStorage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }
}
