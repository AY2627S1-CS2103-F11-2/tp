package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.DeleteCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Verifies the specified add feedback together with filtering, sorting and stable identity. */
public class AddClientWorkflowTest {

    @Test
    public void add_whenIdsExhausted_reportsCommandFailureWithoutChangingData() {
        AddressBook book = new AddressBook();
        book.setNextClientId(Long.MAX_VALUE);
        ModelManager model = new ModelManager(book, new UserPrefs());

        String expectedMessage = "No client was added because no more client IDs are available.";
        assertThrows(CommandException.class, expectedMessage, () -> new AddCommand(person("Alex Tan")).execute(model));

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(Long.MAX_VALUE, model.getAddressBook().getNextClientId());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void add_afterSearch_showsAllSortedClientsAndSelectsStoredRecord() throws Exception {
        ModelManager model = new ModelManager();
        new AddCommand(person("Zara Tan")).execute(model);
        new AddCommand(person("Bob Lim")).execute(model);
        model.updateFilteredPersonList(client -> client.getName().fullName.equals("Zara Tan"));

        CommandResult result = new AddCommand(person("Alex Tan")).execute(model);

        assertEquals("Added client C3: Alex Tan. Current position: C1 (for delete/appointment commands).",
                result.getFeedbackToUser());
        assertEquals(List.of("Alex Tan", "Bob Lim", "Zara Tan"), model.getFilteredPersonList().stream()
                .map(client -> client.getName().fullName).toList());
        Person selected = result.getSelectedClient().orElseThrow();
        assertSame(model.getFilteredPersonList().getFirst(), selected);
        assertEquals("C3", selected.getClientId().orElseThrow().toString());
        assertEquals("Not provided", selected.getEmail().getDisplayValue());
    }

    @Test
    public void add_afterDeletion_usesFreshIdRatherThanDisplayedPosition() throws Exception {
        ModelManager model = new ModelManager();
        Person first = new AddCommand(person("Zara Tan")).execute(model).getSelectedClient().orElseThrow();
        model.deletePerson(first);

        CommandResult result = new AddCommand(person("Alex Tan")).execute(model);

        assertEquals("Added client C2: Alex Tan. Current position: C1 (for delete/appointment commands).",
                result.getFeedbackToUser());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("C2", model.getFilteredPersonList().getFirst().getClientId().orElseThrow().toString());
    }

    @Test
    public void add_reportedPositionTargetsNewClientWhenIdAndPositionDiffer() throws Exception {
        ModelManager model = new ModelManager();
        new AddCommand(person("Zara Tan")).execute(model);
        new AddCommand(person("Bob Lim")).execute(model);

        CommandResult result = new AddCommand(person("Alex Tan")).execute(model);
        String position = result.getFeedbackToUser().split("Current position: ")[1].split(" ")[0];
        new DeleteCommandParser().parse(position).execute(model);

        assertFalse(model.hasPerson(person("Alex Tan")));
        assertEquals(List.of("Bob Lim", "Zara Tan"), model.getFilteredPersonList().stream()
                .map(client -> client.getName().fullName).toList());
    }

    @Test
    public void add_duplicateOutsideSearch_identifiesOriginalClientWithoutAllocatingId() throws Exception {
        ModelManager model = new ModelManager();
        new AddCommand(person("Alex Tan")).execute(model);
        model.updateFilteredPersonList(client -> false);
        Person duplicate = new PersonBuilder(person("Alex Tan")).withPhone("98765432").build();

        String expectedMessage = "Name already belongs to client C1 (Alex Tan). No client was added.";
        assertThrows(CommandException.class, expectedMessage, () -> new AddCommand(duplicate).execute(model));

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(2, model.getAddressBook().getNextClientId());
    }

    private static Person person(String name) {
        return new PersonBuilder().withName(name).withNoEmail().build();
    }
}
