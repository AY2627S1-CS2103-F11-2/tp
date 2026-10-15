package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Failing writes must not silently consume undo/redo or an irreversible deletion boundary. */
public class AppointmentHistorySaveFailureTest {
    @TempDir
    public Path directory;

    @TestFactory
    public Stream<DynamicTest> execute_ioFailure_restoresDataFiltersAndEntireHistory() {
        return Stream.of("undo", "redo", "edit 1 n/Alice Changed", "deleteappt A1 yes", "clear", "delete C1",
                "appointment n/Viewing c/C1 loc/NUS s/20261017-1000 e/20261017-1100")
                .map(command -> dynamicTest(command, () -> assertRollback(command, false)));
    }

    @TestFactory
    public Stream<DynamicTest> execute_permissionFailure_restoresDataFiltersAndEntireHistory() {
        return Stream.of("undo", "redo", "deleteappt A1 yes",
                "appointment n/Viewing c/C1 loc/NUS s/20261017-1000 e/20261017-1100")
                .map(command -> dynamicTest(command, () -> assertRollback(command, true)));
    }

    private void assertRollback(String command, boolean accessDenied) throws Exception {
        ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        FailingStorage disk = new FailingStorage(directory.resolve("schedule.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
        logic.execute("edit 1 n/Alice Updated");
        AddressBook middle = new AddressBook(model.getAddressBook());
        logic.execute("edit 1 p/81234567");
        AddressBook future = new AddressBook(model.getAddressBook());
        logic.execute("undo");
        // Filter the client list to Bob: C1 resolves Bob, while A1 still resolves Alice's viewing.
        model.updateFilteredPersonList(BOB::equals);
        var updatedAlice = model.getAddressBook().getPersonList().get(0);
        model.updateFilteredAppointmentList(updatedAlice, VIEWING.getStart().toLocalDate());
        String originalFile = Files.readString(disk.getAddressBookFilePath());
        disk.failure = accessDenied ? new AccessDeniedException("schedule.json") : new IOException("disk unavailable");

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command));

        assertTrue(exception.getMessage().contains(accessDenied ? "insufficient permissions" : "disk unavailable"));
        assertEquals(middle, model.getAddressBook());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(List.of(VIEWING.withClient(updatedAlice)), model.getFilteredAppointmentList());
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        assertEquals(originalFile, Files.readString(disk.getAddressBookFilePath()));
        disk.failure = null;
        // Both sides of the original timeline must survive a failed branch or history reset.
        logic.execute("redo");
        assertEquals(future, model.getAddressBook());
        logic.execute("undo");
        assertEquals(middle, model.getAddressBook());
        logic.execute("undo");
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(ALICE, model.getAddressBook().getPersonList().get(0));
        logic.execute("redo");
        assertEquals(middle, new AddressBook(disk.readAddressBook().orElseThrow()));
        // Retry the same command successfully using its original displayed references.
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(updatedAlice, VIEWING.getStart().toLocalDate());
        logic.execute(command);
        assertEquals(model.getAddressBook(), new AddressBook(disk.readAddressBook().orElseThrow()));
    }

    private static final class FailingStorage extends JsonAddressBookStorage {
        private IOException failure;

        private FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook book, Path path) throws IOException {
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(book, path);
        }
    }
}
