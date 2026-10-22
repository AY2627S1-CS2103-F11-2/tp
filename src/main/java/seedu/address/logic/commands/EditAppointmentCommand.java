package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;

/** Atomically changes supplied fields of an appointment selected from the displayed list. */
public class EditAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "editappt";
    public static final String MESSAGE_USAGE = "editappt APPOINTMENT_INDEX [n/APPOINTMENT_NAME] "
            + "[c/CLIENT_INDEX] [d/DATETIME] [loc/LOCATION] [desc/DESCRIPTION]";
    public static final String MESSAGE_INVALID_INDEX = "The appointment index provided is invalid. "
            + "(Must be a positive integer between 1 and %d)";
    public static final String MESSAGE_INVALID_CLIENT = "The client index provided is invalid. "
            + "(Must be a positive integer between 1 and %d)";
    public static final String MESSAGE_UNCHANGED =
            "No changes made to appointment %d: supplied values are unchanged.";
    public static final String MESSAGE_TIME_RANGE =
            "The shifted appointment must end within years 0001 to 9999.";

    private static final Logger logger = LogsCenter.getLogger(EditAppointmentCommand.class);
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm");

    private final Index targetIndex;
    private final AppointmentEditFields fields;

    /** Creates a location-only edit using the same validation as other appointment changes. */
    public EditAppointmentCommand(Index targetIndex, String location) {
        this(targetIndex, new AppointmentEditFields(null, null, null, requireNonNull(location), null));
    }

    /** Creates an edit after every supplied field has been validated. */
    public EditAppointmentCommand(Index targetIndex, AppointmentEditFields fields) {
        this.targetIndex = requireNonNull(targetIndex);
        this.fields = requireNonNull(fields);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Appointment> lastShownList = model.getFilteredAppointmentList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.fine("Appointment edit rejected: reference is outside the displayed appointment list.");
            throw new CommandException(String.format(MESSAGE_INVALID_INDEX, lastShownList.size()));
        }
        Appointment target = lastShownList.get(targetIndex.getZeroBased());
        assert model.getAddressBook().getAppointmentList().contains(target)
                : "Displayed appointment must exist in the address book.";
        Appointment edited = createEditedAppointment(model, target);
        if (target.equals(edited)) {
            logger.fine("Appointment edit completed without changes.");
            return CommandResult.withoutSave(String.format(MESSAGE_UNCHANGED, targetIndex.getOneBased()));
        }
        try {
            // The model validates clashes against the full schedule before replacing the target.
            model.setAppointment(target, edited);
        } catch (IllegalArgumentException e) {
            logger.fine("Appointment edit rejected during model validation.");
            throw new CommandException(e.getMessage(), e);
        }
        assert model.getAddressBook().getAppointmentList().contains(edited)
                : "Edited appointment must exist in the address book.";
        logger.info("Updated displayed appointment " + targetIndex.getOneBased()
                + " in memory; awaiting save.");
        return new CommandResult("Edited Appointment: [Index " + targetIndex.getOneBased() + "] Title: "
                + edited.getName() + "; Client: " + edited.getClient().getName().fullName
                + "; Date & Time: " + edited.getStart().format(DISPLAY_DATE_TIME)
                + "; Location: " + edited.getLocation() + "; Description: " + edited.getRemarks());
    }

    private Appointment createEditedAppointment(Model model, Appointment target) throws CommandException {
        Person client = target.getClient();
        if (fields.clientIndex() != null) {
            List<Person> clients = model.getFilteredPersonList();
            if (fields.clientIndex().getZeroBased() >= clients.size()) {
                logger.fine("Appointment edit rejected: client reference is outside the displayed client list.");
                throw new CommandException(String.format(MESSAGE_INVALID_CLIENT, clients.size()));
            }
            client = clients.get(fields.clientIndex().getZeroBased());
        }
        LocalDateTime start = fields.start() == null ? target.getStart() : fields.start();
        LocalDateTime end = target.getEnd();
        if (fields.start() != null) {
            try {
                end = start.plus(Duration.between(target.getStart(), target.getEnd()));
                if (end.getYear() > 9999) {
                    throw new DateTimeException(MESSAGE_TIME_RANGE);
                }
            } catch (DateTimeException | ArithmeticException e) {
                logger.fine("Appointment edit rejected: shifted time is outside the supported range.");
                throw new CommandException(MESSAGE_TIME_RANGE, e);
            }
        }
        assert model.hasPerson(client) : "Appointment client must exist in the address book.";
        return new Appointment(fields.name() == null ? target.getName() : fields.name(), client,
                fields.location() == null ? target.getLocation() : fields.location(), start, end,
                fields.description() == null ? target.getRemarks() : fields.description());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditAppointmentCommand command
                && targetIndex.equals(command.targetIndex) && fields.equals(command.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, fields);
    }
}
