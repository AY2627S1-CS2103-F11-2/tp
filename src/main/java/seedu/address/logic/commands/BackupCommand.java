package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Writes a copy of the current data to the backup folder.
 * It changes no data, no filter and no history entry, so undo and redo are untouched.
 */
public class BackupCommand extends Command {

    public static final String COMMAND_WORD = "backup";
    public static final String MESSAGE_USAGE = "backup [f/FILE]";
    public static final String MESSAGE_SUCCESS = "Backed up %1$d clients to %2$s.";
    public static final String MESSAGE_FAILURE =
            "Could not write the backup to %1$s. Nothing was changed. Reason: %2$s";
    public static final String MESSAGE_EXISTS =
            "A backup named %1$s already exists. Choose another name with f/FILE.";
    public static final Path DEFAULT_BACKUP_DIR = Paths.get("data", "backup");

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm", Locale.ENGLISH);

    private final Optional<String> fileName;
    private final Path backupDir;

    /** Creates a backup command that writes into the default backup folder. */
    public BackupCommand(Optional<String> fileName) {
        this(fileName, DEFAULT_BACKUP_DIR);
    }

    /** Creates a backup command that writes into {@code backupDir}; the second form exists for tests. */
    public BackupCommand(Optional<String> fileName, Path backupDir) {
        this.fileName = requireNonNull(fileName);
        this.backupDir = requireNonNull(backupDir);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Path target = resolveTarget();
        if (Files.exists(target)) {
            throw new CommandException(String.format(MESSAGE_EXISTS, target.getFileName()));
        }
        try {
            Files.createDirectories(backupDir);
            new JsonAddressBookStorage(target).saveAddressBook(model.getAddressBook());
        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_FAILURE, target, e.getMessage()), e);
        }
        int count = model.getAddressBook().getPersonList().size();
        return CommandResult.withoutSave(String.format(MESSAGE_SUCCESS, count, target));
    }

    /** Resolves the file to write, using a timestamped name when none was supplied. */
    private Path resolveTarget() {
        String name = fileName.orElseGet(() -> "clients-" + LocalDateTime.now().format(STAMP) + ".json");
        return backupDir.resolve(name);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BackupCommand command
                && fileName.equals(command.fileName) && backupDir.equals(command.backupDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, backupDir);
    }
}
