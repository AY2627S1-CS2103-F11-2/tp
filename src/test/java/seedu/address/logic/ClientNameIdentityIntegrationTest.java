package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Checks client-name identity through real commands, history and JSON storage. */
public class ClientNameIdentityIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_addLowercaseDuplicate_rejectsWithoutChangingState() throws Exception {
        assertRejectedWithoutChanges("Alex Yeoh", "add n/alex yeoh p/91111111 a/Somewhere y/buyer", false);
    }

    @Test
    public void execute_addCaseAndSpaceDuplicate_rejectsWithoutChangingState() throws Exception {
        assertRejectedWithoutChanges("José Tan", "add n/JOSÉ\u2003  TAN p/91111111 a/Somewhere y/buyer", false);
    }

    @Test
    public void execute_editToOtherClientsName_rejectsWithoutChangingState() throws Exception {
        assertRejectedWithoutChanges("Alex Yeoh", "edit 1 n/BERNICE YU", false);
    }

    @Test
    public void execute_editToHiddenClientsName_rejectsWithoutChangingState() throws Exception {
        assertRejectedWithoutChanges("Alex Yeoh", "edit 1 n/BERNICE YU", true);
    }

    @Test
    public void execute_caseOnlyEdit_persistsWithSameIdAndSupportsUndoRedo() throws Exception {
        Model model = newModel("Alex Yeoh");
        Path dataFile = directory.resolve("addressbook.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(dataFile);
        dataStorage.saveAddressBook(model.getAddressBook());
        Logic logic = logicFor(model, dataStorage);
        Person original = model.getFilteredPersonList().getFirst();

        logic.execute("edit 1 n/ALEX YEOH");

        Person edited = model.getFilteredPersonList().getFirst();
        assertEquals("ALEX YEOH", edited.getName().fullName);
        assertEquals(original.getClientId(), edited.getClientId());
        assertEquals("ALEX YEOH", dataStorage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getName().fullName);
        logic.execute("undo");
        assertEquals(original, model.getFilteredPersonList().getFirst());
        logic.execute("redo");
        assertEquals(edited, model.getFilteredPersonList().getFirst());
        assertEquals(edited.getClientId(), model.getFilteredPersonList().getFirst().getClientId());
    }

    @Test
    public void readAddressBook_legacyCaseVariantDuplicates_rejectsWithoutRewritingFile() throws Exception {
        Path dataFile = directory.resolve("legacy.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(dataFile);
        dataStorage.saveAddressBook(newModel("Alex Yeoh").getAddressBook());
        String legacyJson = Files.readString(dataFile).replace("Bernice Yu", "alex yeoh");
        Files.writeString(dataFile, legacyJson);

        assertThrows(DataLoadingException.class, () -> dataStorage.readAddressBook());

        assertEquals(legacyJson, Files.readString(dataFile));
    }

    private void assertRejectedWithoutChanges(String name, String command, boolean hideDuplicate) throws Exception {
        Model model = newModel(name);
        model.addPerson(new PersonBuilder().withName("Zed Temporary").build());
        model.undoAddressBook();
        if (hideDuplicate) {
            model.updateFilteredPersonList(person -> person.getName().fullName.equals(name));
        }
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        boolean undoBefore = model.canUndoAddressBook();
        Path dataFile = directory.resolve("addressbook.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(dataFile);
        dataStorage.saveAddressBook(model.getAddressBook());
        String savedBefore = Files.readString(dataFile);
        Logic logic = logicFor(model, dataStorage);

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command));

        if (command.startsWith("add ")) {
            assertEquals("Name already belongs to client C1 (" + name + "). No client was added.",
                    exception.getMessage());
        } else {
            assertEquals("This person already exists in the address book.", exception.getMessage());
        }
        assertEquals(before, model.getAddressBook());
        assertEquals(before.getNextClientId(), model.getAddressBook().getNextClientId());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(savedBefore, Files.readString(dataFile));
        assertEquals(undoBefore, model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
    }

    private Model newModel(String firstName) {
        AddressBook data = new AddressBook();
        data.addPerson(new PersonBuilder().withName(firstName).build());
        data.addPerson(new PersonBuilder().withName("Bernice Yu").build());
        return new ModelManager(data, new UserPrefs());
    }

    private Logic logicFor(Model model, JsonAddressBookStorage dataStorage) {
        return new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }
}
