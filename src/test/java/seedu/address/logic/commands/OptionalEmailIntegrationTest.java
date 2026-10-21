package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.logic.parser.EditCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

/**
 * Exercises optional email parsing together with command execution and model updates.
 */
public class OptionalEmailIntegrationTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void add_withoutEmail_addsPersonAndDisplaysNotProvided() throws Exception {
        Person expectedPerson = new PersonBuilder().withNoEmail().build();
        AddCommand command = new AddCommandParser().parse(" " + PersonUtil.getPersonDetails(expectedPerson));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(command, model,
                new CommandResult("Added client C8: Amy Bee. Current position: C2 (for delete/appointment commands).",
                expectedModel.getAddressBook().getPersonList().getLast()),
                expectedModel);
        assertTrue(expectedPerson.getTags().isEmpty());
        assertTrue(Messages.format(expectedPerson).contains("; Email: Not provided;"));
    }

    @Test
    public void add_withEmail_preservesAddressAndDisplaysIt() throws Exception {
        Person expectedPerson = new PersonBuilder().build();
        AddCommand command = new AddCommandParser().parse(" " + PersonUtil.getPersonDetails(expectedPerson));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(command, model,
                new CommandResult("Added client C8: Amy Bee. Current position: C2 (for delete/appointment commands).",
                expectedModel.getAddressBook().getPersonList().getLast()),
                expectedModel);
        assertTrue(Messages.format(expectedPerson).contains("; Email: " + PersonBuilder.DEFAULT_EMAIL + ";"));
    }

    @Test
    public void add_invalidOrRepeatedEmail_doesNotChangeModel() {
        String requiredFields = " " + PersonUtil.getPersonDetails(new PersonBuilder().withNoEmail().build());
        AddressBook original = new AddressBook(model.getAddressBook());
        List<Person> originalFilteredList = new ArrayList<>(model.getFilteredPersonList());
        String[] emailArguments = {" e/", " e/   ", " e/not-an-email", " e/a@example.com e/a@example.com",
            " e/ e/a@example.com"};
        for (String emailArgument : emailArguments) {
            assertThrows(ParseException.class, () -> new AddCommandParser().parse(requiredFields + emailArgument)
                    .execute(model));
            assertEquals(original, model.getAddressBook());
            assertEquals(originalFilteredList, model.getFilteredPersonList());
        }
    }

    @Test
    public void add_duplicateNameWithoutEmail_doesNotChangeModel() throws Exception {
        Person duplicate = new PersonBuilder(ALICE).withNoEmail().build();
        AddCommand command = new AddCommandParser().parse(" " + PersonUtil.getPersonDetails(duplicate));
        assertCommandFailure(command, model,
                "Name already belongs to client C1 (Alice Pauline). No client was added.");
    }

    @Test
    public void edit_omittedEmail_retainsExistingEmail() throws Exception {
        Person expectedPerson = new PersonBuilder(ALICE).withPhone("81234567").build();
        assertEditSuccess("1 p/81234567", expectedPerson);
    }

    @Test
    public void edit_emailCanBeClearedRetainedAndSet() throws Exception {
        Person withoutEmail = new PersonBuilder(ALICE).withNoEmail().build();
        assertEditSuccess("1 e/   ", withoutEmail);
        assertTrue(Messages.format(withoutEmail).contains("; Email: Not provided;"));

        Person changedPhone = new PersonBuilder(withoutEmail).withPhone("81234567").build();
        assertEditSuccess("1 p/81234567", changedPhone);
        assertEditSuccess("1 e/", changedPhone);

        Person withEmail = new PersonBuilder(changedPhone).withEmail("new@example.com").build();
        assertEditSuccess("1 e/new@example.com", withEmail);
    }

    @Test
    public void edit_invalidOrRepeatedEmail_doesNotChangeModel() {
        AddressBook original = new AddressBook(model.getAddressBook());
        List<Person> originalFilteredList = new ArrayList<>(model.getFilteredPersonList());
        String[] inputs = {"1 p/81234567 e/not-an-email", "1 e/ e/a@example.com", "1 e/a@example.com e/"};
        for (String input : inputs) {
            assertThrows(ParseException.class, () -> new EditCommandParser().parse(input).execute(model));
            assertEquals(original, model.getAddressBook());
            assertEquals(originalFilteredList, model.getFilteredPersonList());
        }
    }

    private void assertEditSuccess(String input, Person expectedPerson) throws Exception {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), expectedPerson);
        EditCommand command = new EditCommandParser().parse(input);
        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(expectedPerson)), expectedModel);
    }
}
