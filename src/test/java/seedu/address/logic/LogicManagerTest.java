package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TYPE_DESC_AMY;
import static seedu.address.logic.commands.DeleteCommand.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final String ADD_AMY_COMMAND = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
            + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + TYPE_DESC_AMY;

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete C9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        String expectedMessage = String.format(ListCommand.MESSAGE_SUCCESS, model.getFilteredPersonList().size());
        assertCommandSuccess(listCommand, expectedMessage, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, temporaryFolder.resolve("ExceptionUserPrefs.json"),
                DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, temporaryFolder.resolve("ExceptionUserPrefs.json")));
    }

    @Test
    public void execute_addCommand_logsSavedSuccessWithoutClientDetails() throws Exception {
        try (CapturingLogHandler logs = new CapturingLogHandler()) {
            logic.execute(ADD_AMY_COMMAND);

            assertEquals(List.of("Executing command: AddCommand",
                    "Command changes saved successfully: AddCommand"), logs.getMessages());
            assertEquals(List.of(Level.FINE, Level.INFO), logs.getLevels());
        }
    }

    @Test
    public void execute_storageThrowsIoException_logsRollbackWithoutSavedSuccess() {
        logic = createLogicWithFailingStorage(DUMMY_IO_EXCEPTION);

        try (CapturingLogHandler logs = new CapturingLogHandler()) {
            assertCommandFailure(ADD_AMY_COMMAND, CommandException.class,
                    String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()),
                    new ModelManager());

            assertEquals(List.of("Executing command: AddCommand",
                    "Command changes rolled back after save failure: AddCommand"), logs.getMessages());
            assertEquals(List.of(Level.FINE, Level.WARNING), logs.getLevels());
        }
    }

    @Test
    public void execute_storageThrowsAdException_logsRollbackWithoutSavedSuccess() {
        logic = createLogicWithFailingStorage(DUMMY_AD_EXCEPTION);

        try (CapturingLogHandler logs = new CapturingLogHandler()) {
            assertCommandFailure(ADD_AMY_COMMAND, CommandException.class,
                    String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()),
                    new ModelManager());

            assertEquals(List.of("Executing command: AddCommand",
                    "Command changes rolled back after access-denied save failure: AddCommand"), logs.getMessages());
            assertEquals(List.of(Level.FINE, Level.WARNING), logs.getLevels());
        }
    }

    @Test
    public void execute_appointmentsCommand_logsCompletionWithoutSaving() throws Exception {
        logic = createLogicWithFailingStorage(DUMMY_IO_EXCEPTION);

        try (CapturingLogHandler logs = new CapturingLogHandler()) {
            logic.execute("appointments");

            assertEquals(List.of("Executing command: AppointmentsCommand",
                    "Command completed without saving: AppointmentsCommand"), logs.getMessages());
        }
    }

    @Test
    public void execute_invalidAddCommand_logsParsingRejectionWithoutClientDetails() {
        try (CapturingLogHandler logs = new CapturingLogHandler()) {
            assertThrows(ParseException.class, () -> logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY));

            assertEquals(List.of("Command rejected during parsing."), logs.getMessages());
        }
    }

    @Test
    public void execute_duplicateAddCommand_logsExecutionRejectionWithoutClientDetails() throws Exception {
        logic.execute(ADD_AMY_COMMAND);

        try (CapturingLogHandler logs = new CapturingLogHandler()) {
            assertThrows(CommandException.class, () -> logic.execute(ADD_AMY_COMMAND));

            assertEquals(List.of("Executing command: AddCommand",
                    "Command rejected during execution: AddCommand"), logs.getMessages());
            assertEquals(1, model.getAddressBook().getPersonList().size());
        }
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        logic = createLogicWithFailingStorage(e);

        // Triggers the saveAddressBook method by executing an add command.
        assertCommandFailure(ADD_AMY_COMMAND, CommandException.class, expectedMessage, new ModelManager());
    }

    /** Creates a logic manager whose storage always throws the given exception when saving. */
    private Logic createLogicWithFailingStorage(IOException e) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        return new LogicManager(model, storage);
    }

    /** Captures this component's records without changing logging for other components or tests. */
    private static class CapturingLogHandler extends Handler implements AutoCloseable {
        private final Logger logger = LogsCenter.getLogger(LogicManager.class);
        private final Level previousLevel = logger.getLevel();
        private final List<LogRecord> records = new ArrayList<>();

        CapturingLogHandler() {
            logger.setLevel(Level.ALL);
            logger.addHandler(this);
        }

        List<String> getMessages() {
            return records.stream().map(LogRecord::getMessage).toList();
        }

        List<Level> getLevels() {
            return records.stream().map(LogRecord::getLevel).toList();
        }

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {
            logger.removeHandler(this);
            logger.setLevel(previousLevel);
        }
    }
}
