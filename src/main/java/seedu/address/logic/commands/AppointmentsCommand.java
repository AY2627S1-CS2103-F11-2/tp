package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Shows appointments filtered by displayed client and/or Singapore start date. */
public class AppointmentsCommand extends Command {
    public static final String COMMAND_WORD = "appointments";
    public static final String MESSAGE_USAGE = "appointments [c/CLIENT_INDEX] [d/DATE]";
    public static final String MESSAGE_INVALID_CLIENT = "No client exists at this index in the displayed client list.";
    public static final String MESSAGE_EMPTY = "No appointments found.";

    private static final Logger logger = LogsCenter.getLogger(AppointmentsCommand.class);

    private final Index clientIndex;
    private final LocalDate date;

    /** Creates a filter command; either argument may be null to leave that field unrestricted. */
    public AppointmentsCommand(Index clientIndex, LocalDate date) {
        this.clientIndex = clientIndex;
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person client = null;
        if (clientIndex != null) {
            if (clientIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
                logger.fine("Appointment view rejected: reference is outside the displayed client list.");
                throw new CommandException(MESSAGE_INVALID_CLIENT);
            }
            client = model.getFilteredPersonList().get(clientIndex.getZeroBased());
            assert model.hasPerson(client) : "Displayed client must exist in the address book.";
        }
        model.updateFilteredAppointmentList(client, date);
        int count = model.getFilteredAppointmentList().size();
        assert count <= model.getAddressBook().getAppointmentList().size()
                : "Filtered appointments cannot outnumber stored appointments.";
        logger.fine("Showing " + count + " appointments for client filter "
                + (clientIndex == null ? "none" : "C" + clientIndex.getOneBased())
                + " and date filter " + (date == null ? "none" : date) + ".");
        return new CommandResult(count == 0 ? MESSAGE_EMPTY : "Showing " + count + " appointments.");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AppointmentsCommand command
                && Objects.equals(clientIndex, command.clientIndex) && Objects.equals(date, command.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex, date);
    }
}
