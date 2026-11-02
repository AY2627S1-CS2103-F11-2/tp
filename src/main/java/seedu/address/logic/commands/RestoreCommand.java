package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
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
    public static final String MESSAGE_MISSING = "No backup named %1$s in data/backup. "
            + "Backup names end in .json, for example f/before-july.json.";
    public static final String MESSAGE_NOT_A_LIST = "That file is not a valid client list, so nothing was restored.";
    /** Backups are written and read in the same folder. */
    public static final Path BACKUP_DIR = Paths.get("data", "backup");
    private static final Logger logger = LogsCenter.getLogger(RestoreCommand.class);

    /** Characters Windows forbids in a file name, rejected on every platform so the result is the same. */
    private static final String ILLEGAL_NAME_CHARACTERS = "<>:\"|?*";

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
        if (hasUnusableName(fileName)) {
            throw new CommandException(String.format(MESSAGE_MISSING, fileName));
        }
        Path source;
        try {
            source = backupDir.resolve(fileName);
        } catch (InvalidPathException e) {
            throw new CommandException(String.format(MESSAGE_MISSING, fileName), e);
        }
        if (!Files.exists(source)) {
            throw new CommandException(String.format(MESSAGE_MISSING, fileName));
        }
        logger.fine(() -> "Reading backup " + source);
        Optional<ReadOnlyAddressBook> read;
        try {
            read = new JsonAddressBookStorage(source).readAddressBook();
        } catch (DataLoadingException e) {
            throw new CommandException(MESSAGE_NOT_A_LIST);
        }
        if (read.isEmpty()) {
            throw new CommandException(MESSAGE_NOT_A_LIST);
        }
        assert read.isPresent() : "an absent or empty read was rejected above";
        model.setAddressBook(read.get());
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int count = model.getAddressBook().getPersonList().size();
        logger.info(() -> "Restored " + count + " clients from " + fileName);
        return new CommandResult(String.format(MESSAGE_SUCCESS, count, fileName));
    }

    /** Returns true for a name that cannot be a backup file name, or that points outside the folder. */
    private static boolean hasUnusableName(String name) {
        return name.contains("/") || name.contains("\\") || name.contains("..")
                || name.chars().anyMatch(character -> character < 0x20
                        || ILLEGAL_NAME_CHARACTERS.indexOf(character) >= 0);
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
