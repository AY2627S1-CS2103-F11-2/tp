package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
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

public class RemarkCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addRemark_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person edited = new PersonBuilder(firstPerson).build();
        Person withRemark = new Person(edited.getName(), edited.getPhone(), edited.getEmail(),
                edited.getAddress(), new Remark("Some remark"), edited.getTags());

        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Some remark"));
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(withRemark));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(firstPerson, withRemark);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        RemarkCommand command = new RemarkCommand(outOfBounds, new Remark("x"));
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand standard = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("a"));
        assertTrue(standard.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("a"))));
        assertTrue(standard.equals(standard));
        assertNotEquals(null, standard);
        assertNotEquals(new ClearCommand(), standard);
        assertNotEquals(standard, new RemarkCommand(Index.fromOneBased(2), new Remark("a")));
        assertNotEquals(standard, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("b")));
    }
}
