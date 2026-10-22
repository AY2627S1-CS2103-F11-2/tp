package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.ClientId;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Verifies display ordering independently of storage positions and permanent client IDs. */
public class ClientListOrderTest {

    @Test
    public void displayOrder_alphabeticalIgnoringCaseThenNumericId_preservesStorageOrder() {
        AddressBook book = new AddressBook();
        book.addPerson(person("alex", 10));
        book.addPerson(person("Alex", 2));
        book.addPerson(person("Zara", 11));
        ModelManager model = new ModelManager(book, new UserPrefs());

        assertEquals(List.of(2L, 10L, 11L), ids(model.getFilteredPersonList()));
        assertEquals(List.of(10L, 2L, 11L), ids(model.getAddressBook().getPersonList()));

        model.updateFilteredPersonList(client -> client.getName().fullName.equalsIgnoreCase("alex"));
        assertEquals(List.of(2L, 10L), ids(model.getFilteredPersonList()));
    }

    @Test
    public void renamedClient_movesToCorrectPositionWithoutChangingId() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Zara").build());
        model.addPerson(new PersonBuilder().withName("Bob").build());
        Person zara = model.getFilteredPersonList().getLast();

        model.setPerson(zara, new PersonBuilder(zara).withName("Alex").build());

        assertEquals(List.of("Alex", "Bob"), model.getFilteredPersonList().stream()
                .map(client -> client.getName().fullName).toList());
        assertEquals(List.of(1L, 2L), ids(model.getFilteredPersonList()));
    }

    private static Person person(String name, long id) {
        return new PersonBuilder().withName(name).build().withClientId(new ClientId(id));
    }

    private static List<Long> ids(List<Person> clients) {
        return clients.stream().map(client -> client.getClientId().orElseThrow().getNumber()).toList();
    }
}
