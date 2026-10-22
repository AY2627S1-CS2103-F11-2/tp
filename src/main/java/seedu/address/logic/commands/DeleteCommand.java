package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a client identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the client identified by its position in the displayed client list.\n"
            + "Parameters: CLIENT_ID (C followed directly by a positive integer, e.g. C1)\n"
            + "Example: " + COMMAND_WORD + " C1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted Client: %1$s";
    public static final String MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX =
            "The client index provided is invalid. Choose a client from the currently displayed list.";

    private static final Logger logger = LogsCenter.getLogger(DeleteCommand.class);

    private final Index targetIndex;

    /**
     * Creates a deletion for a displayed client reference.
     *
     * @throws NullPointerException if {@code targetIndex} is null.
     */
    public DeleteCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.fine("Client deletion rejected: reference is outside the displayed client list.");
            throw new CommandException(MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        assert model.hasPerson(personToDelete) : "Displayed client must exist in the address book.";
        model.deletePerson(personToDelete);
        assert !model.hasPerson(personToDelete) : "Deleted client must no longer exist in the address book.";
        assert model.getAddressBook().getAppointmentList().stream()
                .noneMatch(appointment -> appointment.getClient().equals(personToDelete))
                : "Deleted client must not have any remaining linked appointments.";
        logger.info("Deleted displayed client C" + targetIndex.getOneBased() + " in memory; awaiting save.");
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
