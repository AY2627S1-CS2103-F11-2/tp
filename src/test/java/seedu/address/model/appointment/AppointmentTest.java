package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class AppointmentTest {
    @Test
    public void constructor_validFields_trimsEdgesPreservesInternalSpacesAndRemarks() {
        Appointment appointment = new Appointment("  Viewing  flat  ", ALICE, "  NUS  123  ",
                VIEWING.getStart(), VIEWING.getEnd(), "Bring keys");
        assertEquals("Viewing  flat", appointment.getName());
        assertEquals("NUS  123", appointment.getLocation());
        assertEquals("Bring keys", appointment.getRemarks());
    }

    @Test
    public void constructor_invalidFields_rejects() {
        for (String name : new String[]{"", " ", "Flat1", "View!", "a".repeat(81), "View\nflat"}) {
            assertThrows(IllegalArgumentException.class, () -> new Appointment(name, ALICE, "NUS",
                    VIEWING.getStart(), VIEWING.getEnd(), ""));
        }
        for (String location : new String[]{"", " ", "\t\n", "\u2003"}) {
            assertThrows(IllegalArgumentException.class, () -> new Appointment("Viewing", ALICE, location,
                    VIEWING.getStart(), VIEWING.getEnd(), ""));
        }
        assertTrue(Appointment.isValidName("a".repeat(80)));
        assertThrows(NullPointerException.class, () -> VIEWING.withClient(null));
    }

    @Test
    public void constructor_nonblankLocations_preservesPunctuationAndInternalWhitespace() {
        for (String location : new String[]{"NUS COM3 #02-01", "NUS  COM3 #02-01", "NUS\n123", "#02-01"}) {
            Appointment appointment = new Appointment("Viewing", ALICE, "\u2003 " + location + " \u2003",
                    VIEWING.getStart(), VIEWING.getEnd(), "");
            assertEquals(location, appointment.getLocation());
        }
    }

    @Test
    public void constructor_endNotAfterStart_rejects() {
        for (LocalDateTime end : new LocalDateTime[]{VIEWING.getStart(), VIEWING.getStart().minusMinutes(1)}) {
            assertThrows(IllegalArgumentException.class, () -> new Appointment("Viewing", ALICE, "NUS",
                    VIEWING.getStart(), end, ""));
        }
    }

    @Test
    public void overlaps_handlesContainmentPartialOverlapAndAdjacentSlots() {
        assertTrue(VIEWING.overlaps(VIEWING));
        Appointment contained = new Appointment("Call", BOB, "NUS", VIEWING.getStart().plusMinutes(10),
                VIEWING.getEnd().minusMinutes(10), "");
        assertTrue(VIEWING.overlaps(contained));
        assertTrue(contained.overlaps(VIEWING));
        Appointment partial = new Appointment("Call", BOB, "NUS", VIEWING.getStart().minusMinutes(10),
                VIEWING.getStart().plusMinutes(10), "");
        assertTrue(VIEWING.overlaps(partial));
        Appointment adjacent = new Appointment("Call", BOB, "NUS", VIEWING.getEnd(),
                VIEWING.getEnd().plusHours(1), "");
        assertFalse(VIEWING.overlaps(adjacent));
        assertFalse(adjacent.overlaps(VIEWING));
        assertFalse(VIEWING.overlaps(SIGNING));
    }

    @Test
    public void equals_andWithClient_compareAllFields() {
        Appointment copy = VIEWING.withClient(ALICE);
        assertEquals(VIEWING, copy);
        assertEquals(VIEWING.hashCode(), copy.hashCode());
        assertNotEquals(VIEWING, VIEWING.withClient(BOB));
        assertNotEquals(VIEWING, SIGNING);
        assertNotEquals(VIEWING, null);
        assertNotEquals(VIEWING, "Viewing");
    }
}
