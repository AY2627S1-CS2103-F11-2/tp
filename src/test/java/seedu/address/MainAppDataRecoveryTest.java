package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises startup recovery using real invalid data files without launching a JavaFX window. */
public class MainAppDataRecoveryTest {
    @TempDir
    public Path directory;

    @Test
    public void startup_caseVariantDuplicateNames_explainsCauseAndPreservesOriginal() throws Exception {
        Path dataFile = directory.resolve("addressbook.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(dataFile);
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName("Alex Yeoh").build());
        book.addPerson(new PersonBuilder().withName("Bernice Yu").build());
        dataStorage.saveAddressBook(book);
        String original = Files.readString(dataFile).replace("Bernice Yu", "alex yeoh");
        Files.writeString(dataFile, original);

        MainApp app = startModel(dataStorage);

        String message = startupMessage(app);
        assertTrue(message.contains("duplicate client names"), message);
        assertTrue(message.contains("letter case"), message);
        assertTrue(message.contains("Make the names distinct"), message);
        assertTrue(message.contains("was renamed to"), message);
        assertFalse(message.contains("Alex Yeoh"), message);
        assertFalse(message.contains("alex yeoh"), message);
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(dataFile));
        List<Path> preserved;
        try (var files = Files.list(directory)) {
            preserved = files.filter(path -> path.getFileName().toString().contains(".corrupt-")).toList();
        }
        assertEquals(1, preserved.size());
        assertEquals(original, Files.readString(preserved.getFirst()));
        assertTrue(message.contains(preserved.getFirst().toString()), message);
        dataStorage.saveAddressBook(app.model.getAddressBook());
        assertEquals(original, Files.readString(preserved.getFirst()));
    }

    @Test
    public void startup_malformedJson_doesNotClaimNamesAreDuplicated() throws Exception {
        Path dataFile = directory.resolve("addressbook.json");
        Files.writeString(dataFile, "{ invalid json");

        MainApp app = startModel(new JsonAddressBookStorage(dataFile));

        String message = startupMessage(app);
        assertTrue(message.contains("could not be read"), message);
        assertFalse(message.contains("duplicate client names"), message);
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
    }

    private MainApp startModel(JsonAddressBookStorage dataStorage) throws Exception {
        MainApp app = new MainApp();
        app.storage = new StorageManager(dataStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        Method initialize = MainApp.class.getDeclaredMethod("initModelManager", Storage.class, ReadOnlyUserPrefs.class);
        initialize.setAccessible(true);
        app.model = (Model) initialize.invoke(app, app.storage, new UserPrefs());
        return app;
    }

    private String startupMessage(MainApp app) throws Exception {
        Field field = MainApp.class.getDeclaredField("startupMessage");
        field.setAccessible(true);
        return (String) field.get(app);
    }
}
