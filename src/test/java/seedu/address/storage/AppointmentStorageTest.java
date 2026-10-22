package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AppointmentStorageTest {
    @TempDir
    public Path directory;

    @Test
    public void saveAndReload_preservesAllAppointmentFieldsAndEditedClientLinks() throws Exception {
        AddressBook book = getAppointmentAddressBook();
        Path file = directory.resolve("schedule.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(book);
        assertEquals(book, new AddressBook(storage.readAddressBook().orElseThrow()));
        Person edited = new PersonBuilder(ALICE).withName("Alice Updated").build();
        book.setPerson(ALICE, edited);
        book.removeAppointment(SIGNING);
        storage.saveAddressBook(book);
        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(book, loaded);
        assertTrue(loaded.getAppointmentList().contains(VIEWING.withClient(edited)));
        book.removePerson(edited);
        storage.saveAddressBook(book);
        assertTrue(storage.readAddressBook().orElseThrow().getAppointmentList().isEmpty());
    }

    @Test
    public void readLegacyClientOnlyFile_defaultsToEmptySchedule() throws Exception {
        Path file = directory.resolve("legacy.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getAppointmentAddressBook());
        ObjectNode root = JsonUtil.fromJsonString(Files.readString(file), ObjectNode.class);
        root.remove("appointments");
        Files.writeString(file, root.toString());
        assertEquals(2, storage.readAddressBook().orElseThrow().getPersonList().size());
        assertTrue(storage.readAddressBook().orElseThrow().getAppointmentList().isEmpty());
    }

    @Test
    public void saveAndReload_locationWithUnitNumberAndUnicodePadding_preservesNormalizedLocation() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addAppointment(new Appointment("Viewing", ALICE,
                "\u2003 NUS  COM3 #02-01 \u2003", VIEWING.getStart(), VIEWING.getEnd(), ""));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("unit-number.json"));
        storage.saveAddressBook(book);
        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(book, loaded);
        assertEquals("NUS  COM3 #02-01", loaded.getAppointmentList().getFirst().getLocation());
    }

    @Test
    public void invalidAppointment_rejectsWholeFileWithoutChangingIt() throws Exception {
        Path file = directory.resolve("invalid.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getAppointmentAddressBook());
        String valid = Files.readString(file);
        for (String field : new String[]{"name", "clientIndex", "location", "start", "end"}) {
            ObjectNode root = JsonUtil.fromJsonString(valid, ObjectNode.class);
            ObjectNode appointment = (ObjectNode) root.get("appointments").get(0);
            appointment.remove(field);
            Files.writeString(file, root.toString());
            assertThrows(DataLoadingException.class, storage::readAddressBook, field);
            assertEquals(root.toString(), Files.readString(file));
        }
        for (String fragment : new String[]{"{\"clientIndex\":0}", "{\"clientIndex\":3}",
            "{\"clientIndex\":1.5}", "{\"clientIndex\":2147483648}", "{\"name\":\"\"}",
            "{\"location\":\"\"}", "{\"location\":\"\\u2003\"}",
            "{\"start\":\"2026-02-30T10:00\"}", "{\"end\":\"2000-01-01T10:00\"}"}) {
            ObjectNode root = JsonUtil.fromJsonString(valid, ObjectNode.class);
            ObjectNode appointment = (ObjectNode) root.get("appointments").get(0);
            appointment.setAll(JsonUtil.fromJsonString(fragment, ObjectNode.class));
            Files.writeString(file, root.toString());
            assertThrows(DataLoadingException.class, storage::readAddressBook, fragment);
        }
        ObjectNode root = JsonUtil.fromJsonString(valid, ObjectNode.class);
        ArrayNode appointments = (ArrayNode) root.get("appointments");
        appointments.add(appointments.get(0));
        Files.writeString(file, root.toString());
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        appointments.removeAll().addNull();
        Files.writeString(file, root.toString());
        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void adaptAppointment_clientMissingFromSavedList_failsAssertion() {
        assertThrows(AssertionError.class, () -> new JsonAdaptedAppointment(VIEWING, List.of()));
    }
}
