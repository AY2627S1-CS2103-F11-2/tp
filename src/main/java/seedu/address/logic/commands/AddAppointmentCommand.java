package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;

/** Creates an appointment so the schedule can be used without hand-editing stored data. */
public class AddAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "appointment";
    public static final String MESSAGE_USAGE =
            "appointment n/NAME c/CLIENT_INDEX loc/LOCATION s/YYYYMMDD-HHMM e/YYYYMMDD-HHMM";

    private final String name;
    private final Index clientIndex;
    private final String location;
    private final LocalDateTime start;
    private final LocalDateTime end;

    /** Creates a command with validated appointment fields and a displayed client reference. */
    public AddAppointmentCommand(String name, Index clientIndex, String location,
            LocalDateTime start, LocalDateTime end) {
        this.name = requireNonNull(name);
        this.clientIndex = requireNonNull(clientIndex);
        this.location = requireNonNull(location);
        this.start = requireNonNull(start);
        this.end = requireNonNull(end);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (clientIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(AppointmentsCommand.MESSAGE_INVALID_CLIENT);
        }
        Appointment appointment = new Appointment(name, model.getFilteredPersonList().get(clientIndex.getZeroBased()),
                location, start, end, "");
        for (Appointment existing : model.getAddressBook().getAppointmentList()) {
            if (appointment.overlaps(existing)) {
                throw new CommandException("Schedule clash with appointment: " + existing.getName()
                        + " (" + existing.getStart() + " to " + existing.getEnd() + "). Nothing was added.");
            }
        }
        model.addAppointment(appointment);
        int displayedIndex = model.getFilteredAppointmentList().indexOf(appointment) + 1;
        return new CommandResult("Added appointment A" + displayedIndex + " : " + name + " with "
                + appointment.getClient().getName().fullName + " from " + start + " to " + end + " at " + location);
    }
}
