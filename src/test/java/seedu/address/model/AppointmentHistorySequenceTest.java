package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.exceptions.NoRedoableStateException;
import seedu.address.model.exceptions.NoUndoableStateException;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Compares long, reproducible mixed command sequences against an independent value-based timeline. */
public class AppointmentHistorySequenceTest {
    @TestFactory
    public Stream<DynamicTest> mixedOperations_matchExpectedDataAndHistoryAtEveryStep() {
        return Stream.of(17, 64, 71, 2103).map(seed -> dynamicTest("seed=" + seed, () -> assertSequence(seed)));
    }

    private void assertSequence(int seed) {
        ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        List<ExpectedData> timeline = new ArrayList<>();
        timeline.add(new ExpectedData(List.copyOf(model.getAddressBook().getPersonList()),
                List.copyOf(model.getAddressBook().getAppointmentList())));
        int pointer = 0;
        Random random = new Random(seed);
        for (int step = 0; step < 250; step++) {
            ExpectedData before = timeline.get(pointer);
            List<Person> persons = new ArrayList<>(before.persons);
            List<Appointment> appointments = new ArrayList<>(before.appointments);
            int operation = random.nextInt(11);
            boolean irreversible = false;
            if (operation == 7) {
                if (pointer > 0) {
                    model.undoAddressBook();
                    pointer--;
                } else {
                    assertThrows(NoUndoableStateException.class, model::undoAddressBook);
                }
            } else if (operation == 8) {
                if (pointer + 1 < timeline.size()) {
                    model.redoAddressBook();
                    pointer++;
                } else {
                    assertThrows(NoRedoableStateException.class, model::redoAddressBook);
                }
            } else {
                if (operation == 5 || persons.isEmpty()) {
                    Person added = new PersonBuilder().withName("Client " + letters(seed * 1000 + step)).build();
                    model.addPerson(added);
                    persons.add(added);
                } else if (operation == 0 || operation == 1 && appointments.isEmpty()) {
                    Person client = persons.get(random.nextInt(persons.size()));
                    Appointment added = appointment(client, step);
                    model.addAppointment(added);
                    appointments.add(added);
                } else if (operation == 1) {
                    int index = random.nextInt(appointments.size());
                    Appointment edited = appointment(persons.get(random.nextInt(persons.size())), step);
                    model.setAppointment(appointments.get(index), edited);
                    appointments.set(index, edited);
                } else if (operation == 2) {
                    int index = random.nextInt(persons.size());
                    Person target = persons.get(index);
                    Person edited = new PersonBuilder(target).withPhone(Integer.toString(80000000 + step)).build();
                    model.setPerson(target, edited);
                    persons.set(index, edited);
                    appointments.replaceAll(a -> a.getClient().equals(target) ? a.withClient(edited) : a);
                } else if (operation == 3 && !appointments.isEmpty()) {
                    Appointment target = appointments.remove(random.nextInt(appointments.size()));
                    model.deleteAppointment(target);
                    irreversible = true;
                } else if (operation == 4) {
                    Person target = persons.remove(random.nextInt(persons.size()));
                    model.deletePerson(target);
                    appointments.removeIf(a -> a.getClient().equals(target));
                } else if (operation == 6) {
                    model.setAddressBook(new AddressBook());
                    persons.clear();
                    appointments.clear();
                } else if (operation == 9 && !appointments.isEmpty()) {
                    Appointment target = appointments.get(random.nextInt(appointments.size()));
                    model.setAppointment(target, target);
                } else if (operation == 10 && !appointments.isEmpty()) {
                    assertThrows(IllegalArgumentException.class, () -> model.addAppointment(appointments.get(0)));
                }
                ExpectedData after = new ExpectedData(List.copyOf(persons), List.copyOf(appointments));
                if (irreversible) {
                    timeline.clear();
                    timeline.add(after);
                    pointer = 0;
                } else if (!before.equals(after)) {
                    timeline.subList(pointer + 1, timeline.size()).clear();
                    timeline.add(after);
                    pointer++;
                }
            }
            String context = "seed=" + seed + ", step=" + step + ", operation=" + operation;
            ExpectedData expected = timeline.get(pointer);
            assertEquals(expected.persons, model.getAddressBook().getPersonList(), context);
            assertEquals(expected.appointments, model.getAddressBook().getAppointmentList(), context);
            assertEquals(expected.appointments.stream().sorted(Appointment.CHRONOLOGICAL_ORDER).toList(),
                    model.getFilteredAppointmentList(), context);
            assertEquals(pointer > 0, model.canUndoAddressBook(), context);
            assertEquals(pointer + 1 < timeline.size(), model.canRedoAddressBook(), context);
        }
    }

    private Appointment appointment(Person client, int step) {
        LocalDateTime start = LocalDateTime.of(2027, 1, 1, 10, 0).plusDays(step);
        return new Appointment("Viewing " + letters(step), client, "Block " + step + " #02-01",
                start, start.plusHours(1));
    }

    private String letters(int value) {
        StringBuilder result = new StringBuilder();
        do {
            result.append((char) ('a' + value % 26));
            value /= 26;
        } while (value > 0);
        return result.toString();
    }

    private record ExpectedData(List<Person> persons, List<Appointment> appointments) {}
}
