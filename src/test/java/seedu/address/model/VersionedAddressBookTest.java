package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.Test;

import seedu.address.model.exceptions.NoRedoableStateException;
import seedu.address.model.exceptions.NoUndoableStateException;

public class VersionedAddressBookTest {

    private final ReadOnlyAddressBook emptyAddressBook = new AddressBook();

    @Test
    public void commit_noDataChange_doesNotCreateAnUndoableState() {
        VersionedAddressBook addressBook = new VersionedAddressBook(emptyAddressBook);

        addressBook.commit();

        assertFalse(addressBook.canUndo());
        assertFalse(addressBook.canRedo());
    }

    @Test
    public void commit_dataChange_canBeUndoneAndRedone() {
        VersionedAddressBook addressBook = new VersionedAddressBook(emptyAddressBook);
        addressBook.addPerson(ALICE);
        addressBook.commit();

        assertTrue(addressBook.canUndo());

        addressBook.undo();
        assertEquals(0, addressBook.getPersonList().size());
        assertTrue(addressBook.canRedo());

        addressBook.redo();
        assertEquals(1, addressBook.getPersonList().size());
        assertEquals(ALICE, addressBook.getPersonList().get(0));
        assertFalse(addressBook.canRedo());
    }

    @Test
    public void commit_changeAfterUndo_discardsTheRedoableStates() {
        VersionedAddressBook addressBook = new VersionedAddressBook(emptyAddressBook);
        addressBook.addPerson(ALICE);
        addressBook.commit();
        addressBook.undo();
        assertTrue(addressBook.canRedo());

        addressBook.addPerson(BENSON);
        addressBook.commit();

        assertFalse(addressBook.canRedo());
        assertEquals(BENSON, addressBook.getPersonList().get(0));
    }

    @Test
    public void undo_noEarlierState_throwsNoUndoableStateException() {
        VersionedAddressBook addressBook = new VersionedAddressBook(emptyAddressBook);

        assertThrows(NoUndoableStateException.class, addressBook::undo);
    }

    @Test
    public void redo_noUndoneState_throwsNoRedoableStateException() {
        VersionedAddressBook addressBook = new VersionedAddressBook(emptyAddressBook);

        assertThrows(NoRedoableStateException.class, addressBook::redo);
    }
}
