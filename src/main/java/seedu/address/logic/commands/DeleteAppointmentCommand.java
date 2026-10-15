package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;

/** Deletes one appointment from the displayed list after the parser has checked confirmation. */
public class DeleteAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "deleteappt";
    public static final String MESSAGE_USAGE = "deleteappt A<INDEX> yes";
    public static final String MESSAGE_INVALID_INDEX =
            "No appointment exists at this index in the displayed appointment list.";

    private final Index targetIndex;

    public DeleteAppointmentCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (targetIndex.getZeroBased() >= model.getFilteredAppointmentList().size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        Appointment target = model.getFilteredAppointmentList().get(targetIndex.getZeroBased());
        model.deleteAppointment(target);
        String feedback = "Deleted appointment A" + targetIndex.getOneBased() + ". This action cannot be undone.";
        if (model.getFilteredAppointmentList().isEmpty()) {
            feedback += "\n" + AppointmentsCommand.MESSAGE_EMPTY;
        }
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteAppointmentCommand command
                && targetIndex.equals(command.targetIndex);
    }

    @Override
    public int hashCode() {
        return targetIndex.hashCode();
    }
}
