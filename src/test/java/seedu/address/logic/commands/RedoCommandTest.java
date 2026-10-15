package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class RedoCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_nothingToRedo_throwsCommandException() {
        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterUndo_reappliesTheChange() throws Exception {
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        new DeleteCommand(INDEX_FIRST_PERSON).execute(expectedModel); // the state redo should reach
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        new UndoCommand().execute(model);

        assertCommandSuccess(new RedoCommand(), model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_newChangeAfterUndo_nothingToRedo() throws Exception {
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        new UndoCommand().execute(model);
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }
}
