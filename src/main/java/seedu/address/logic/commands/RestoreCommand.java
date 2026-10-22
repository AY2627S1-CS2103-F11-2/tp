package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Replaces all client and appointment data with a backup file from the backup folder.
 * The replacement is one undoable step, so a mistaken restore can be undone.
 */
public class RestoreCommand extends Command {

    public static final String COMMAND_WORD = "restore";
    public static final String MESSAGE_USAGE = "restore f/FILE";
    public static final String MESSAGE_SUCCESS = "Restored %1$d clients from %2$s";
    public static final String MESSAGE_MISSING = "No backup file named %1$s in the data folder.";
    public static final String MESSAGE_NOT_A_LIST = "That file is not a valid client list, so nothing was restored.";
    /** Backups are written and read in the same folder. */
    public static final Path BACKUP_DIR = Paths.get("data", "backup");

    private final String fileName;
    private final Path backupDir;

    /** Creates a restore command that reads from the default backup folder. */
    public RestoreCommand(String fileName) {
        this(fileName, BACKUP_DIR);
    }

    /** Creates a restore command that reads from {@code backupDir}; the second form exists for tests. */
    public RestoreCommand(String fileName, Path backupDir) {
        this.fileName = requireNonNull(fileName);
        this.backupDir = requireNonNull(backupDir);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            throw new CommandException(String.format(MESSAGE_MISSING, fileName));
        }
        Path source = backupDir.resolve(fileName);
        if (!Files.exists(source)) {
            throw new CommandException(String.format(MESSAGE_MISSING, fileName));
        }
        Optional<ReadOnlyAddressBook> read;
        try {
            read = new JsonAddressBookStorage(source).readAddressBook();
        } catch (DataLoadingException e) {
            throw new CommandException(MESSAGE_NOT_A_LIST);
        }
        if (read.isEmpty()) {
            throw new CommandException(MESSAGE_NOT_A_LIST);
        }
        model.setAddressBook(read.get());
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int count = model.getAddressBook().getPersonList().size();
        return new CommandResult(String.format(MESSAGE_SUCCESS, count, fileName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RestoreCommand command
                && fileName.equals(command.fileName) && backupDir.equals(command.backupDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, backupDir);
    }
}
