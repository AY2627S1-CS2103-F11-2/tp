package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.ClientId;
import seedu.address.model.person.Person;

public class ClientIdStorageTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void roundTrip_deletedHighestIdAndAppointment_preservesIdsAndCounter() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BOB);
        book.addAppointment(VIEWING);
        book.removePerson(BOB);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        storage.saveAddressBook(book);

        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(book, loaded);
        assertEquals(new ClientId(1), loaded.getAppointmentList().get(0).getClient().getClientId().orElseThrow());
        loaded.addPerson(BENSON);
        assertEquals(new ClientId(3), loaded.getPersonList().get(1).getClientId().orElseThrow());
    }

    @Test
    public void legacyJson_assignsDeterministicIdsInOriginalOrder() throws Exception {
        String json = legacyJson(List.of(ALICE, BOB));
        AddressBook migrated = read(json);
        assertEquals(new ClientId(1), migrated.getPersonList().get(0).getClientId().orElseThrow());
        assertEquals(new ClientId(2), migrated.getPersonList().get(1).getClientId().orElseThrow());
        assertEquals(3, migrated.getNextClientId());
        assertEquals(migrated, read(JsonUtil.toJsonString(new JsonSerializableAddressBook(migrated))));
    }

    @Test
    public void mixedJson_reservesExplicitIdBeforeMigratingEarlierClient() throws Exception {
        String json = legacyJson(List.of(ALICE, BOB.withClientId(new ClientId(1))));
        AddressBook migrated = read(json);
        assertEquals(new ClientId(2), migrated.getPersonList().get(0).getClientId().orElseThrow());
        assertEquals(new ClientId(1), migrated.getPersonList().get(1).getClientId().orElseThrow());
    }

    @Test
    public void duplicateIds_rejectedEvenWhenClientNamesDiffer() throws Exception {
        String json = legacyJson(List.of(ALICE.withClientId(new ClientId(1)), BOB.withClientId(new ClientId(1))));
        assertThrows(IllegalValueException.class, () -> read(json));
    }

    @Test
    public void malformedIds_rejectedWithoutNumericCoercion() throws Exception {
        String base = JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE));
        for (String invalid : List.of("0", "-1", "1.5", "\"1\"", "9223372036854775808")) {
            String person = base.replace("\"clientId\" : null", "\"clientId\" : " + invalid);
            String json = "{\"persons\":[" + person + "]}";
            assertThrows(IllegalValueException.class, () -> read(json), invalid);
        }
    }

    @Test
    public void malformedCounterAndCounterBehindExistingId_rejected() throws Exception {
        String person = JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE.withClientId(new ClientId(3))));
        for (String invalid : List.of("0", "-1", "3", "1.5", "\"4\"", "9223372036854775808")) {
            String json = "{\"persons\":[" + person + "],\"nextClientId\":" + invalid + "}";
            assertThrows(IllegalValueException.class, () -> read(json), invalid);
        }
    }

    @Test
    public void emptyBookWithPersistedCounter_doesNotRestartNumbering() throws Exception {
        AddressBook loaded = read("{\"persons\":[],\"nextClientId\":50}");
        loaded.addPerson(ALICE);
        assertEquals(new ClientId(50), loaded.getPersonList().get(0).getClientId().orElseThrow());
    }

    private static String legacyJson(List<Person> persons) throws Exception {
        return JsonUtil.toJsonString(new JsonSerializableAddressBook(
                persons.stream().map(JsonAdaptedPerson::new).toList(), null));
    }

    private static AddressBook read(String json) throws Exception {
        return JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
    }
}
