package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TYPE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                new CommandResult("Added client C8: Amy Bee. Current position: C2 (for delete/appointment commands).",
                        expectedModel.getAddressBook().getPersonList().getLast()),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                "Name already belongs to client C1 (Alice Pauline). No client was added.");
    }

    @Test
    public void execute_parsedUnicodeNameWithSpaces_addsNormalizedPerson() throws Exception {
        AddCommand command = new AddCommandParser().parse(" n/  李   D'Arcy-Smith  " + PHONE_DESC_BOB
                + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TYPE_DESC_BOB);
        Person expectedPerson = new PersonBuilder(BOB).withName("李 D'Arcy-Smith").withTags().build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(command, model, new CommandResult("Added client C8: 李 D'Arcy-Smith. "
                + "Current position: C8 (for delete/appointment commands).",
                expectedModel.getAddressBook().getPersonList().getLast()),
                expectedModel);
    }

    @Test
    public void execute_duplicateNormalizedNameOutsideFilteredList_doesNotChangeModel() throws Exception {
        model.updateFilteredPersonList(person -> false);
        AddCommand command = new AddCommandParser().parse(" n/  Alice   Pauline  " + PHONE_DESC_BOB
                + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TYPE_DESC_BOB);

        assertCommandFailure(command, model,
                "Name already belongs to client C1 (Alice Pauline). No client was added.");
    }

}
