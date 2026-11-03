package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises filtered client edits through real commands, observable lists and file storage. */
public class EditClientFilterIntegrationTest {
    @TempDir
    public Path directory;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage dataStorage;

    @Test
    public void execute_editStillMatchingName_keepsOtherClientsHidden() throws Exception {
        initialize();
        logic.execute("search n/Alice");
        Person original = model.getFilteredPersonList().getFirst();

        logic.execute("edit 1 n/Alice Lim");

        assertEquals(List.of("Alice Lim"), displayedNames());
        assertEquals(original.getClientId(), model.getFilteredPersonList().getFirst().getClientId());
        assertEquals(model.getAddressBook(), dataStorage.readAddressBook().orElseThrow());
        assertThrows(CommandException.class, () -> logic.execute("edit 2 p/94567890"));
        assertEquals(List.of("Alice Lim"), displayedNames());
        logic.execute("list");
        assertEquals(List.of("Alice Lim", "Bob Lim"), displayedNames());
    }

    @Test
    public void execute_editNoLongerMatchingName_leavesEmptySearchResults() throws Exception {
        assertEditLeavesSearch("search n/Alice", "edit 1 n/Carol Ong");
    }

    @Test
    public void execute_editNoLongerMatchingPhone_leavesEmptySearchResults() throws Exception {
        assertEditLeavesSearch("search p/91234567", "edit 1 p/93456789");
    }

    @Test
    public void execute_editNoLongerMatchingType_leavesEmptySearchResults() throws Exception {
        assertEditLeavesSearch("search y/buyer", "edit 1 y/landlord");
    }

    @Test
    public void execute_saveFailure_restoresClientAndSearchResults() throws Exception {
        initialize();
        logic.execute("search n/Alice");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        Path dataFile = dataStorage.getAddressBookFilePath();
        Files.delete(dataFile);
        Files.createDirectory(dataFile);

        assertThrows(CommandException.class, () -> logic.execute("edit 1 n/Carol Ong"));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertTrue(Files.isDirectory(dataFile));
    }

    private void assertEditLeavesSearch(String search, String edit) throws Exception {
        initialize();
        logic.execute(search);
        Person original = model.getFilteredPersonList().getFirst();

        logic.execute(edit);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(original.getClientId(), model.getAddressBook().getPersonList().getFirst().getClientId());
        AddressBook after = new AddressBook(model.getAddressBook());
        assertEquals(after, dataStorage.readAddressBook().orElseThrow());
        logic.execute("undo");
        assertEquals(List.of("Alice Tan", "Bob Lim"), displayedNames());
        assertEquals(original, model.getFilteredPersonList().getFirst());
        logic.execute("redo");
        assertEquals(after, model.getAddressBook());
        assertEquals(2, model.getFilteredPersonList().size());
    }

    private void initialize() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName("Alice Tan").withPhone("91234567").withType("buyer").build());
        book.addPerson(new PersonBuilder().withName("Bob Lim").withPhone("92345678").withType("seller").build());
        model = new ModelManager(book, new UserPrefs());
        dataStorage = new JsonAddressBookStorage(directory.resolve("addressbook.json"));
        dataStorage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }

    private List<String> displayedNames() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }
}
