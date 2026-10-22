package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;

/** Changes only the location of an appointment in the displayed list. */
public class EditAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "editappt";
    public static final String MESSAGE_USAGE = "editappt A<INDEX> loc/LOCATION";
    public static final String MESSAGE_INVALID_INDEX =
            "No appointment exists at this index in the displayed appointment list.";
    public static final String MESSAGE_UNCHANGED = "The appointment already has this location. Nothing was changed.";

    private static final Logger logger = LogsCenter.getLogger(EditAppointmentCommand.class);

    private final Index targetIndex;
    private final String location;

    /** Creates a location-only edit for a displayed appointment reference. */
    public EditAppointmentCommand(Index targetIndex, String location) {
        this.targetIndex = requireNonNull(targetIndex);
        this.location = requireNonNull(location).strip();
        if (!Appointment.isValidLocation(this.location)) {
            logger.fine("Appointment location edit rejected: blank location.");
            throw new IllegalArgumentException(Appointment.MESSAGE_INVALID_LOCATION);
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Appointment> lastShownList = model.getFilteredAppointmentList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.fine("Appointment location edit rejected: reference is outside the displayed appointment list.");
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        Appointment target = lastShownList.get(targetIndex.getZeroBased());
        assert model.getAddressBook().getAppointmentList().contains(target)
                : "Displayed appointment must exist in the address book.";
        if (target.getLocation().equals(location)) {
            logger.fine("Appointment location edit rejected: location is unchanged.");
            throw new CommandException(MESSAGE_UNCHANGED);
        }
        Appointment edited = new Appointment(target.getName(), target.getClient(), location,
                target.getStart(), target.getEnd(), target.getRemarks());
        model.setAppointment(target, edited);
        assert model.getAddressBook().getAppointmentList().contains(edited)
                : "Edited appointment must exist in the address book.";
        logger.info("Updated location of displayed appointment A" + targetIndex.getOneBased()
                + " in memory; awaiting save.");
        return new CommandResult("Updated appointment A" + targetIndex.getOneBased() + " ("
                + edited.getName() + ") location: " + edited.getLocation());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditAppointmentCommand command
                && targetIndex.equals(command.targetIndex) && location.equals(command.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, location);
    }
}
