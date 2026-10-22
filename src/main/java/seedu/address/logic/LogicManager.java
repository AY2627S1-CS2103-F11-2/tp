package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AppointmentsCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT =
            "Could not write to the data file at %1$s. Your last change is not saved. (%2$s)";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not write to the data file at %1$s. Your last change is not saved, because there are "
                    + "insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        CommandResult commandResult;
        Command command;
        try {
            command = addressBookParser.parseCommand(commandText);
        } catch (ParseException e) {
            logger.fine("Command rejected during parsing.");
            throw e;
        }
        String commandType = command.getClass().getSimpleName();
        logger.fine("Executing command: " + commandType);
        Model.Checkpoint checkpoint = model.createCheckpoint();
        try {
            commandResult = command.execute(model);
        } catch (CommandException e) {
            logger.fine("Command rejected during execution: " + commandType);
            throw e;
        }

        if (command instanceof AppointmentsCommand) {
            logger.info("Command completed without saving: " + commandType);
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            model.restoreCheckpoint(checkpoint);
            logger.warning("Command changes rolled back after access-denied save failure: " + commandType);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT,
                    storage.getAddressBookFilePath()), e);
        } catch (IOException ioe) {
            model.restoreCheckpoint(checkpoint);
            logger.warning("Command changes rolled back after save failure: " + commandType);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT,
                    storage.getAddressBookFilePath(), ioe.getMessage()), ioe);
        }

        logger.info("Command changes saved successfully: " + commandType);
        return commandResult;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ObservableList<Appointment> getFilteredAppointmentList() {
        return model.getFilteredAppointmentList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
