package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.ClientId;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

public class StableClientIdTest {

    @Test
    public void addPerson_assignsSequentialIdsAndFailedDuplicateDoesNotConsumeOne() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        assertThrows(DuplicatePersonException.class, () -> book.addPerson(ALICE));
        book.addPerson(BOB);

        assertEquals(new ClientId(1), book.getPersonList().get(0).getClientId().orElseThrow());
        assertEquals(new ClientId(2), book.getPersonList().get(1).getClientId().orElseThrow());
        assertEquals(3, book.getNextClientId());
        assertFalse(ALICE.getClientId().isPresent());
    }

    @Test
    public void editPerson_preservesIdAndUpdatesAppointmentReference() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addAppointment(VIEWING);
        Person edited = new PersonBuilder(ALICE).withName("Alice Changed").build().withClientId(new ClientId(90));

        book.setPerson(ALICE, edited);

        assertEquals(new ClientId(1), book.getPersonList().get(0).getClientId().orElseThrow());
        assertEquals(book.getPersonList().get(0).getClientId(),
                book.getAppointmentList().get(0).getClient().getClientId());
        assertEquals(2, book.getNextClientId());
    }

    @Test
    public void removeHighestId_copyAndAdd_doNotReuseDeletedId() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BOB);
        book.removePerson(BOB);

        AddressBook copy = new AddressBook(book);
        copy.addPerson(BENSON);

        assertEquals(new ClientId(3), copy.getPersonList().get(1).getClientId().orElseThrow());
        assertEquals(4, copy.getNextClientId());
        assertEquals(3, book.getNextClientId());
    }

    @Test
    public void setPersons_reservesExplicitIdsBeforeMigratingUnassignedClients() {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(ALICE, BOB.withClientId(new ClientId(1))));

        assertEquals(new ClientId(2), book.getPersonList().get(0).getClientId().orElseThrow());
        assertEquals(new ClientId(1), book.getPersonList().get(1).getClientId().orElseThrow());
        assertEquals(3, book.getNextClientId());
    }

    @Test
    public void invalidIdsAndCounters_rejectedWithoutChangingData() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        AddressBook before = new AddressBook(book);

        assertThrows(IllegalArgumentException.class, () -> book.setPersons(List.of(
                ALICE.withClientId(new ClientId(1)), BOB.withClientId(new ClientId(1)))));
        assertThrows(IllegalArgumentException.class, () -> book.setNextClientId(1));
        assertThrows(IllegalArgumentException.class, () -> book.setNextClientId(0));
        assertEquals(before, book);
    }

    @Test
    public void equals_includesClientIdsAndAllocationCounter() {
        AddressBook first = new AddressBook();
        first.addPerson(ALICE);
        AddressBook otherId = new AddressBook();
        otherId.addPerson(ALICE.withClientId(new ClientId(2)));
        AddressBook otherCounter = new AddressBook(first);
        otherCounter.setNextClientId(10);

        assertEquals(first.getPersonList(), otherId.getPersonList());
        assertNotEquals(first, otherId);
        assertNotEquals(first, otherCounter);
        assertEquals(first, new AddressBook(first));
    }

    @Test
    public void undoRedoAndClear_keepIdsAndNeverReuseAllocatedNumber() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook());
        book.addPerson(ALICE);
        book.commit();
        book.undo();
        book.redo();
        assertEquals(new ClientId(1), book.getPersonList().get(0).getClientId().orElseThrow());
        book.undo();
        book.addPerson(BOB);
        book.commit();
        assertEquals(new ClientId(2), book.getPersonList().get(0).getClientId().orElseThrow());
        book.resetData(new AddressBook());
        book.addPerson(BENSON);
        assertEquals(new ClientId(3), book.getPersonList().get(0).getClientId().orElseThrow());
    }

    @Test
    public void commit_afterUndoWithoutDataChange_doesNotCreateHistory() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook());
        book.addPerson(ALICE);
        book.commit();
        book.undo();
        book.commit();

        assertFalse(book.canUndo());
        book.redo();
        assertEquals(new ClientId(1), book.getPersonList().get(0).getClientId().orElseThrow());
    }

    @Test
    public void restoreState_checkpointAfterUndo_keepsHighWaterMarkAndRedoAfterNoOpCommit() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook());
        book.addPerson(ALICE);
        book.commit();
        book.addPerson(BOB);
        book.commit();
        book.undo();
        VersionedAddressBook.State checkpoint = book.captureState();
        book.addPerson(BENSON);
        book.commit();

        book.restoreState(checkpoint);
        book.commit();
        book.redo();

        assertEquals(List.of(ALICE, BOB), book.getPersonList());
        assertEquals(new ClientId(2), book.getPersonList().get(1).getClientId().orElseThrow());
        assertEquals(3, book.getNextClientId());
    }

    @Test
    public void restoreState_afterFailedSave_restoresCounterAndHistoryExactly() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook());
        book.addPerson(ALICE);
        book.commit();
        VersionedAddressBook.State checkpoint = book.captureState();
        book.addPerson(BOB);
        book.commit();

        book.restoreState(checkpoint);
        assertEquals(2, book.getNextClientId());
        assertEquals(List.of(ALICE), book.getPersonList());
        assertFalse(book.canRedo());
        book.addPerson(BENSON);
        assertEquals(new ClientId(2), book.getPersonList().get(1).getClientId().orElseThrow());
    }
}
