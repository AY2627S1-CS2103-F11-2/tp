package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.HANDOVER;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AppointmentModelTest {
    private final ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());

    @Test
    public void appointmentList_sortedUnmodifiableAndLive() {
        assertEquals(List.of(VIEWING, SIGNING, HANDOVER), model.getFilteredAppointmentList());
        assertThrows(UnsupportedOperationException.class, () -> model.getFilteredAppointmentList().clear());
        assertThrows(UnsupportedOperationException.class, () -> model.getAddressBook().getAppointmentList().clear());
        var liveList = model.getFilteredAppointmentList();
        model.deleteAppointment(VIEWING);
        assertEquals(List.of(SIGNING, HANDOVER), liveList);
    }

    @Test
    public void filters_combineAndReplaceWithoutChangingClients() {
        model.updateFilteredPersonList(p -> p.equals(BOB));
        model.updateFilteredAppointmentList(ALICE, LocalDate.of(2026, 10, 15));
        assertEquals(List.of(VIEWING), model.getFilteredAppointmentList());
        model.updateFilteredAppointmentList(null, LocalDate.of(2026, 10, 16));
        assertEquals(List.of(HANDOVER), model.getFilteredAppointmentList());
        model.updateFilteredAppointmentList(BOB, null);
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        model.updateFilteredAppointmentList(null, null);
        assertEquals(3, model.getFilteredAppointmentList().size());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
    }

    @Test
    public void editClient_updatesLinksAndRetainsClientDateFilter() {
        model.updateFilteredAppointmentList(ALICE, VIEWING.getStart().toLocalDate());
        Person edited = new PersonBuilder(ALICE).withName("Alice Updated").withPhone("12345678").build();
        model.setPerson(ALICE, edited);
        assertEquals(List.of(VIEWING.withClient(edited)), model.getFilteredAppointmentList());
        assertTrue(model.getAddressBook().getAppointmentList().contains(HANDOVER.withClient(edited)));
        assertTrue(model.getAddressBook().getAppointmentList().contains(SIGNING));
    }

    @Test
    public void deleteClient_cascadesOnlyLinkedAppointments() {
        model.updateFilteredAppointmentList(ALICE, VIEWING.getStart().toLocalDate());
        model.deletePerson(ALICE);
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        assertEquals(List.of(SIGNING), model.getAddressBook().getAppointmentList());
        assertEquals(List.of(BOB), model.getAddressBook().getPersonList());
    }

    @Test
    public void deleteAppointment_retainsClientsAndRestoresFullSchedule() {
        model.updateFilteredAppointmentList(BOB, null);
        model.deleteAppointment(SIGNING);
        assertEquals(List.of(VIEWING, HANDOVER), model.getFilteredAppointmentList());
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
    }

    @Test
    public void resetAndCopy_includeAppointmentsAndClearRemovesThem() {
        AddressBook source = getAppointmentAddressBook();
        AddressBook copy = new AddressBook(source);
        assertEquals(source, copy);
        assertEquals(source.hashCode(), copy.hashCode());
        copy.resetData(copy);
        assertEquals(source, copy);
        copy.removeAppointment(VIEWING);
        assertNotEquals(source, copy);
        model.setAddressBook(new AddressBook());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.getFilteredAppointmentList().isEmpty());
        assertEquals(3, source.getAppointmentList().size());
    }

    @Test
    public void addAppointment_duplicateOrMissingClient_rejectedWithoutMutation() {
        AddressBook book = getAppointmentAddressBook();
        assertThrows(IllegalArgumentException.class, () -> book.addAppointment(VIEWING));
        book.removePerson(ALICE);
        assertThrows(IllegalArgumentException.class, () -> book.addAppointment(VIEWING));
        assertThrows(IllegalArgumentException.class, () -> book.removeAppointment(VIEWING));
        assertEquals(List.of(SIGNING), book.getAppointmentList());
    }
}
