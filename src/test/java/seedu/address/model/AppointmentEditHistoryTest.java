package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.HANDOVER;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Exercises the atomic model operation intended for the future editappt command. */
public class AppointmentEditHistoryTest {
    private final ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());

    @Test
    public void setAppointment_allFieldsChanged_singleUndoStepAndFilteredChronologicalDisplay() {
        Appointment edited = new Appointment("Inspection", BOB, "NUS COM3 #02-01",
                HANDOVER.getEnd(), HANDOVER.getEnd().plusHours(2), "New keys");
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(ALICE, VIEWING.getStart().toLocalDate());
        var liveSchedule = model.getFilteredAppointmentList();

        model.setAppointment(VIEWING, edited);

        assertTrue(liveSchedule.isEmpty());
        assertTrue(model.getAddressBook().getAppointmentList().contains(edited));
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        model.undoAddressBook();
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
        model.redoAddressBook();
        assertEquals(List.of(SIGNING, HANDOVER, edited), liveSchedule);
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void setAppointment_ownTimeUnchanged_excludesTargetFromClashCheck() {
        Appointment edited = new Appointment("Inspection", BOB, VIEWING.getLocation(),
                VIEWING.getStart(), VIEWING.getEnd(), "");
        model.setAppointment(VIEWING, edited);
        assertEquals(List.of(edited, SIGNING, HANDOVER), model.getFilteredAppointmentList());
        model.undoAddressBook();
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
    }

    @Test
    public void setAppointment_locationOnly_preservesBothFiltersForEditappt() {
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(BOB, SIGNING.getStart().toLocalDate());
        Appointment edited = new Appointment(SIGNING.getName(), BOB, "NUS COM3 #02-01",
                SIGNING.getStart(), SIGNING.getEnd(), SIGNING.getRemarks());
        model.setAppointment(SIGNING, edited);
        assertEquals(List.of(edited), model.getFilteredAppointmentList());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        model.undoAddressBook();
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        model.redoAddressBook();
        assertEquals(List.of(VIEWING, edited, HANDOVER), model.getFilteredAppointmentList());
    }

    @Test
    public void setAppointment_adjacentToOtherAppointments_allowed() {
        Appointment edited = new Appointment("Inspection", ALICE, "NUS",
                SIGNING.getStart().minusHours(1), SIGNING.getStart(), "");
        model.setAppointment(VIEWING, edited);
        assertTrue(model.getAddressBook().getAppointmentList().contains(edited));
        Appointment after = new Appointment("Inspection", ALICE, "NUS",
                SIGNING.getEnd(), SIGNING.getEnd().plusHours(1), "");
        model.setAppointment(edited, after);
        assertTrue(model.getAddressBook().getAppointmentList().contains(after));
    }

    @Test
    public void setAppointment_clashesAcrossClientsOrContainsOtherAppointment_rejectedAtomically() {
        List<Appointment> invalid = List.of(SIGNING,
                new Appointment("Inspection", ALICE, "NUS", SIGNING.getStart().minusMinutes(1),
                        SIGNING.getEnd().plusMinutes(1), ""),
                new Appointment("Inspection", ALICE, "NUS", SIGNING.getStart().plusMinutes(1),
                        SIGNING.getEnd().minusMinutes(1), ""),
                new Appointment("Inspection", ALICE, "NUS", SIGNING.getStart().minusMinutes(1),
                        SIGNING.getStart().plusMinutes(1), ""),
                new Appointment("Inspection", ALICE, "NUS", SIGNING.getEnd().minusMinutes(1),
                        SIGNING.getEnd().plusMinutes(1), ""));
        prepareRedoAndFilters();
        for (Appointment replacement : invalid) {
            assertThrows(IllegalArgumentException.class, () -> model.setAppointment(VIEWING, replacement));
            assertUnchangedWithRedoAndFilters();
        }
        model.redoAddressBook();
        assertEquals("Alice Updated", model.getAddressBook().getPersonList().get(0).getName().fullName);
    }

    @Test
    public void setAppointment_missingClientOrTarget_rejectedAtomically() {
        prepareRedoAndFilters();
        assertThrows(IllegalArgumentException.class, () -> model.setAppointment(VIEWING, VIEWING.withClient(CARL)));
        Appointment missing = new Appointment("Missing", ALICE, "NUS", HANDOVER.getEnd(),
                HANDOVER.getEnd().plusHours(1), "");
        assertThrows(IllegalArgumentException.class, () -> model.setAppointment(missing, missing));
        assertUnchangedWithRedoAndFilters();
    }

    @Test
    public void setAppointment_nullArguments_rejectedAtomically() {
        prepareRedoAndFilters();
        assertThrows(NullPointerException.class, () -> model.setAppointment(null, VIEWING));
        assertThrows(NullPointerException.class, () -> model.setAppointment(VIEWING, null));
        assertUnchangedWithRedoAndFilters();
    }

    @Test
    public void setAppointment_identicalReplacement_preservesRedoAndFilters() {
        prepareRedoAndFilters();
        Appointment copy = new Appointment(VIEWING.getName(), ALICE, VIEWING.getLocation(),
                VIEWING.getStart(), VIEWING.getEnd(), VIEWING.getRemarks());
        model.setAppointment(VIEWING, copy);
        assertUnchangedWithRedoAndFilters();
    }

    @Test
    public void setAppointment_changeAfterUndo_discardsRedo() {
        prepareRedoAndFilters();
        Appointment edited = new Appointment("Inspection", BOB, "NUS", VIEWING.getStart(), VIEWING.getEnd(), "");
        model.setAppointment(VIEWING, edited);
        assertFalse(model.canRedoAddressBook());
        model.undoAddressBook();
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void setAppointment_thenEditClientAndDeleteClient_historyRestoresConsistentLinks() {
        Appointment edited = VIEWING.withClient(BOB);
        model.setAppointment(VIEWING, edited);
        Person updatedBob = new PersonBuilder(BOB).withName("Bob Updated").build();
        model.setPerson(BOB, updatedBob);
        AddressBook beforeDelete = new AddressBook(model.getAddressBook());
        model.deletePerson(updatedBob);
        assertEquals(List.of(HANDOVER), model.getAddressBook().getAppointmentList());
        model.undoAddressBook();
        assertEquals(beforeDelete, model.getAddressBook());
        model.undoAddressBook();
        assertTrue(model.getAddressBook().getAppointmentList().contains(edited));
        assertTrue(model.getAddressBook().getAppointmentList().contains(SIGNING));
        model.undoAddressBook();
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        model.redoAddressBook();
        model.redoAddressBook();
        assertEquals(beforeDelete, model.getAddressBook());
        model.redoAddressBook();
        assertEquals(List.of(HANDOVER), model.getAddressBook().getAppointmentList());
    }

    @Test
    public void addAppointment_clash_rejectedWithoutDiscardingRedo() {
        prepareRedoAndFilters();
        Appointment clash = new Appointment("Inspection", BOB, "Other location", VIEWING.getStart(),
                VIEWING.getEnd(), "");
        assertThrows(IllegalArgumentException.class, () -> model.addAppointment(clash));
        assertUnchangedWithRedoAndFilters();
    }

    @Test
    public void deleteAppointment_missingTarget_doesNotClearHistory() {
        prepareRedoAndFilters();
        LocalDateTime later = HANDOVER.getEnd().plusDays(1);
        Appointment missing = new Appointment("Missing", ALICE, "NUS", later, later.plusHours(1), "");
        assertThrows(IllegalArgumentException.class, () -> model.deleteAppointment(missing));
        assertUnchangedWithRedoAndFilters();
    }

    private void prepareRedoAndFilters() {
        model.setPerson(ALICE, new PersonBuilder(ALICE).withName("Alice Updated").build());
        model.undoAddressBook();
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(ALICE, VIEWING.getStart().toLocalDate());
    }

    private void assertUnchangedWithRedoAndFilters() {
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(List.of(VIEWING), model.getFilteredAppointmentList());
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
    }
}
