package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class ModelCheckpointTest {
    private final ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());

    @Test
    public void restoreCheckpoint_afterHistoryBoundary_restoresBothDirectionsAndFilters() {
        model.setAppointment(VIEWING, VIEWING.withClient(BOB));
        AddressBook middle = new AddressBook(model.getAddressBook());
        model.deletePerson(ALICE);
        AddressBook future = new AddressBook(model.getAddressBook());
        model.undoAddressBook();
        model.updateFilteredPersonList(BOB::equals);
        model.updateFilteredAppointmentList(BOB, SIGNING.getStart().toLocalDate());
        Model.Checkpoint checkpoint = model.createCheckpoint();
        model.deleteAppointment(SIGNING);
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());

        model.restoreCheckpoint(checkpoint);

        assertEquals(middle, model.getAddressBook());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(List.of(VIEWING.withClient(BOB), SIGNING), model.getFilteredAppointmentList());
        model.redoAddressBook();
        assertEquals(future, model.getAddressBook());
        model.undoAddressBook();
        model.undoAddressBook();
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
    }

    @Test
    public void restoreCheckpoint_reusableAfterBranchTruncation_keepsCapturedSnapshotsIndependent() {
        model.setPerson(ALICE, new PersonBuilder(ALICE).withName("Alice Updated").build());
        model.undoAddressBook();
        Model.Checkpoint checkpoint = model.createCheckpoint();
        for (int i = 0; i < 3; i++) {
            model.setAppointment(VIEWING, VIEWING.withClient(BOB));
            assertFalse(model.canRedoAddressBook());
            model.restoreCheckpoint(checkpoint);
            assertEquals(getAppointmentAddressBook(), model.getAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertFalse(model.canUndoAddressBook());
        }
    }

    @Test
    public void restoreCheckpoint_initialNullPredicate_restoresAllClients() {
        Model.Checkpoint checkpoint = model.createCheckpoint();
        model.updateFilteredPersonList(p -> false);
        model.updateFilteredAppointmentList(BOB, null);
        model.restoreCheckpoint(checkpoint);
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
        assertEquals(3, model.getFilteredAppointmentList().size());
    }

    @Test
    public void restoreCheckpoint_nullForeignOrUnknownCheckpoint_rejectedWithoutMutation() {
        ModelManager another = new ModelManager();
        assertThrows(NullPointerException.class, () -> model.restoreCheckpoint(null));
        assertThrows(IllegalArgumentException.class, () -> model.restoreCheckpoint(another.createCheckpoint()));
        assertThrows(IllegalArgumentException.class, () -> model.restoreCheckpoint(new Model.Checkpoint() {}));
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
    }
}
