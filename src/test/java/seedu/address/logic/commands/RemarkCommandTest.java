package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests updating remarks through the command and Model.
 */
public class RemarkCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RemarkCommand(null, new Remark("")));
        assertThrows(NullPointerException.class, () -> new RemarkCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_addRemark_success() {
        assertRemarkSuccess(INDEX_FIRST_PERSON, "Likes baseball", RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void execute_replaceRemark_success() {
        setFirstPersonRemark("Old note");
        assertRemarkSuccess(INDEX_FIRST_PERSON, "Call after 6 pm", RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void execute_removeRemark_success() {
        setFirstPersonRemark("Call after 6 pm");
        assertRemarkSuccess(INDEX_FIRST_PERSON, "", RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS);
    }

    @Test
    public void execute_validIndexFilteredList_updatesDisplayedPerson() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess(INDEX_FIRST_PERSON, "Prefers email", RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(outOfBoundIndex, new Remark("New note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("New note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("New note"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("New note"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("New note"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Other note"))));
    }

    /**
     * Sets up an existing remark on the first person.
     */
    private void setFirstPersonRemark(String remark) {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(person, new PersonBuilder(person).withRemark(remark).build());
    }

    /**
     * Asserts that the remark changes while every other field and contact stays unchanged.
     */
    private void assertRemarkSuccess(Index index, String remark, String message) {
        Person personToEdit = model.getFilteredPersonList().get(index.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withRemark(remark).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(new RemarkCommand(index, new Remark(remark)), model,
                String.format(message, Messages.format(editedPerson)), expectedModel);
        assertEquals(editedPerson.getType(), personToEdit.getType());
    }
}
