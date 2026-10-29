package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;

/** Creates an appointment so the schedule can be used without hand-editing stored data. */
public class AddAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "appointment";
    public static final String MESSAGE_USAGE =
            "appointment n/NAME c/CLIENT_INDEX loc/LOCATION s/YYYYMMDD-HHMM e/YYYYMMDD-HHMM";
    public static final String MESSAGE_SUCCESS = "Added appointment A%1$d: %2$s with %3$s, %4$s to %5$s, at %6$s.";
    public static final String MESSAGE_CLASH = "Schedule clash with appointment: %1$s (%2$s to %3$s). "
            + "Nothing was added.";

    /** Matches the date-time layout of the appointment cards. */
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final Logger logger = LogsCenter.getLogger(AddAppointmentCommand.class);

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
            logger.fine("Appointment creation rejected: reference is outside the displayed client list.");
            throw new CommandException(AppointmentsCommand.MESSAGE_INVALID_CLIENT);
        }
        Appointment appointment = new Appointment(name, model.getFilteredPersonList().get(clientIndex.getZeroBased()),
                location, start, end, "");
        assert model.hasPerson(appointment.getClient()) : "Displayed client must exist in the address book.";
        for (Appointment existing : model.getAddressBook().getAppointmentList()) {
            if (appointment.overlaps(existing)) {
                logger.fine("Appointment creation rejected: the new time overlaps a stored appointment.");
                throw new CommandException(String.format(MESSAGE_CLASH, existing.getName(),
                        existing.getStart().format(DISPLAY_DATE_TIME), existing.getEnd().format(DISPLAY_DATE_TIME)));
            }
        }
        model.addAppointment(appointment);
        assert model.getAddressBook().getAppointmentList().contains(appointment)
                : "Created appointment must be stored in the address book.";
        int displayedIndex = model.getFilteredAppointmentList().indexOf(appointment) + 1;
        assert displayedIndex > 0 : "Created appointment must be shown in the displayed appointment list.";
        logger.info("Created appointment A" + displayedIndex + " for displayed client C"
                + clientIndex.getOneBased() + " in memory; awaiting save.");
        return new CommandResult(String.format(MESSAGE_SUCCESS, displayedIndex, appointment.getName(),
                appointment.getClient().getName().fullName, start.format(DISPLAY_DATE_TIME),
                end.format(DISPLAY_DATE_TIME), appointment.getLocation()));
    }
}
