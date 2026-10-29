package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.FileUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    private String startupMessage = "";

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath(), startupMessage);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * or an empty address book will be used instead if errors occur when reading {@code storage}'s address book.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using the application data file.");

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new application data file populated with sample data.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            logger.warning("The application data file could not be loaded. Starting with empty data.");
            startupMessage = Messages.formatUnreadableDataFile(storage.getAddressBookFilePath(),
                    preserveUnreadableDataFile());
            initialData = new AddressBook();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Moves a data file that could not be read aside, so that starting the app and then saving cannot
     * overwrite data that is still recoverable by hand.
     */
    private Optional<Path> preserveUnreadableDataFile() {
        Path dataFilePath = storage.getAddressBookFilePath();
        try {
            Path quarantinedFilePath = FileUtil.quarantineCorruptFile(dataFilePath);
            logger.warning("The unreadable application data file was quarantined.");
            return Optional.of(quarantinedFilePath);
        } catch (IOException e) {
            logger.warning("The unreadable application data file could not be quarantined.");
            return Optional.empty();
        }
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using the application preferences file.");

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating a new application preferences file.");
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("The application preferences file could not be loaded. Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save application preferences.");
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save application preferences during shutdown.");
        }
    }
}
