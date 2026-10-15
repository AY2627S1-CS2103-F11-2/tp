package seedu.address.testutil;

import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.LocalDateTime;

import seedu.address.model.AddressBook;
import seedu.address.model.appointment.Appointment;

/** Appointments deliberately stored out of chronological order to exercise displayed references. */
public class TypicalAppointments {
    public static final Appointment VIEWING = new Appointment("Viewing", ALICE, "Clementi 123456",
            LocalDateTime.of(2026, 10, 15, 10, 0), LocalDateTime.of(2026, 10, 15, 11, 0), "Bring keys");
    public static final Appointment SIGNING = new Appointment("Signing", BOB, "NUS",
            LocalDateTime.of(2026, 10, 15, 14, 0), LocalDateTime.of(2026, 10, 15, 15, 0), "");
    public static final Appointment HANDOVER = new Appointment("Handover", ALICE, "Sentosa Cove",
            LocalDateTime.of(2026, 10, 16, 9, 0), LocalDateTime.of(2026, 10, 16, 10, 0), "Check inventory");

    public static AddressBook getAppointmentAddressBook() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BOB);
        book.addAppointment(HANDOVER);
        book.addAppointment(SIGNING);
        book.addAppointment(VIEWING);
        return book;
    }
}
