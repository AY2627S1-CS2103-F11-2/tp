package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.HANDOVER;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddAppointmentCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class AppointmentCommandsTest {
    private final ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());

    @Test
    public void appointments_singleMatch_usesTheSingularWord() throws Exception {
        model.deleteAppointment(HANDOVER);
        model.deleteAppointment(VIEWING);
        assertEquals("Showing 1 appointment.",
                new AppointmentsCommand(null, null).execute(model).getFeedbackToUser());
    }

    @Test
    public void appointments_usesDisplayedClientAndReplacesPreviousFilter() throws Exception {
        model.updateFilteredPersonList(p -> p.equals(BOB));
        new AppointmentsCommand(Index.fromOneBased(1), LocalDate.of(2026, 10, 15)).execute(model);
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        assertEquals("Showing 3 appointments.", new AppointmentsCommand(null, null).execute(model).getFeedbackToUser());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals("No appointments found.", new AppointmentsCommand(null, LocalDate.of(2000, 1, 1))
                .execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredAppointmentList().isEmpty());
    }

    @Test
    public void appointments_outOfRange_preservesDataAndBothFilters() {
        model.updateFilteredPersonList(p -> p.equals(BOB));
        model.updateFilteredAppointmentList(ALICE, null);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> new AppointmentsCommand(Index.fromOneBased(2), null).execute(model));
        assertEquals(List.of(VIEWING, HANDOVER), model.getFilteredAppointmentList());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void deleteAppointment_usesFilteredIndexThenShowsFullScheduleAndRetainsClient() throws Exception {
        model.updateFilteredAppointmentList(BOB, null);
        String feedback = new DeleteAppointmentCommand(Index.fromOneBased(1)).execute(model).getFeedbackToUser();
        assertEquals("Deleted appointment A1. This action cannot be undone.", feedback);
        assertEquals(List.of(VIEWING, HANDOVER), model.getFilteredAppointmentList());
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
        // The next A1 now identifies the chronologically first remaining appointment.
        new DeleteAppointmentCommand(Index.fromOneBased(1)).execute(model);
        assertEquals(List.of(HANDOVER), model.getFilteredAppointmentList());
        assertEquals("Deleted appointment A1. This action cannot be undone.\nNo appointments found.",
                new DeleteAppointmentCommand(Index.fromOneBased(1)).execute(model).getFeedbackToUser());
        assertThrows(CommandException.class, () -> new DeleteAppointmentCommand(Index.fromOneBased(1)).execute(model));
    }

    @Test
    public void deleteAppointment_outOfRangeOrEmpty_doesNotMutateOrClearFilter() {
        model.updateFilteredAppointmentList(BOB, null);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> new DeleteAppointmentCommand(Index.fromOneBased(2)).execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        model.updateFilteredAppointmentList(null, LocalDate.of(2000, 1, 1));
        assertThrows(CommandException.class, () -> new DeleteAppointmentCommand(Index.fromOneBased(1)).execute(model));
        assertTrue(model.getFilteredAppointmentList().isEmpty());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void addAppointment_filteredClientAndAdjacentTimes_succeeds() throws Exception {
        model.updateFilteredPersonList(p -> p.equals(BOB));
        new AddAppointmentCommandParser().parse(" n/Call c/C1 loc/NUS s/20261015-1100 e/20261015-1200")
                .execute(model);
        assertEquals(4, model.getAddressBook().getAppointmentList().size());
        assertEquals(BOB, model.getFilteredAppointmentList().get(1).getClient());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
    }

    @Test
    public void addAppointment_clashWithHiddenAppointmentOrMissingClient_noMutation() throws Exception {
        model.updateFilteredAppointmentList(BOB, null);
        AddressBook before = new AddressBook(model.getAddressBook());
        AddAppointmentCommandParser parser = new AddAppointmentCommandParser();
        for (String args : new String[]{" n/Call c/C1 loc/NUS s/20261015-1030 e/20261015-1130",
            " n/Call c/C3 loc/NUS s/20261015-1100 e/20261015-1200"}) {
            Command command = parser.parse(args);
            assertThrows(CommandException.class, () -> command.execute(model));
            assertEquals(before, model.getAddressBook());
            assertEquals(List.of(SIGNING), model.getFilteredAppointmentList());
        }
    }
}
