package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AppointmentNameTest {
    @Test
    public void constructor_validName_trimsEdges() {
        AppointmentName name = new AppointmentName("  Viewing  flat  ");
        assertEquals("Viewing  flat", name.getValue());
        assertEquals("Viewing  flat", name.toString());
    }

    @Test
    public void constructor_invalidName_rejects() {
        for (String value : new String[]{"", " ", "View!", "a".repeat(81), "View\nflat", "1".repeat(51)}) {
            assertThrows(IllegalArgumentException.class, () -> new AppointmentName(value));
        }
        assertThrows(NullPointerException.class, () -> new AppointmentName(null));
    }

    @Test
    public void constructor_boundaryLengthNames_areHandledCorrectly() {
        assertEquals("A", new AppointmentName("A").getValue());
        assertEquals("A".repeat(80), new AppointmentName("A".repeat(80)).getValue());
        assertThrows(IllegalArgumentException.class, () -> new AppointmentName("A".repeat(81)));
    }

    @Test
    public void isValid_validatesNameConstraints() {
        assertEquals(true, AppointmentName.isValid("Viewing flat"));
        assertEquals(true, AppointmentName.isValid("A".repeat(80)));
        assertEquals(false, AppointmentName.isValid(""));
        assertEquals(true, AppointmentName.isValid("Flat1"));
        assertEquals(false, AppointmentName.isValid("A".repeat(81)));
        assertEquals(false, AppointmentName.isValid(null));
    }

    @Test
    public void editedNames_roundTripWithoutChangingCreationRules() {
        assertEquals("Viewing 123", new AppointmentName("Viewing 123").getValue());
        assertEquals("1".repeat(50), new AppointmentName("1".repeat(50)).getValue());
        assertEquals(true, AppointmentName.isValidForEditing("Viewing 123"));
        assertEquals(false, AppointmentName.isValidForEditing("A".repeat(51)));
        assertEquals(false, AppointmentName.isValidForCreation("Viewing 123"));
        assertEquals(true, AppointmentName.isValidForCreation("A".repeat(80)));
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        assertEquals(new AppointmentName("Viewing"), new AppointmentName(" Viewing "));
        assertEquals(new AppointmentName("Viewing").hashCode(), new AppointmentName("Viewing").hashCode());
        assertNotEquals(new AppointmentName("Viewing"), new AppointmentName("Viewing flat"));
        assertNotEquals(new AppointmentName("Viewing"), null);
        assertNotEquals(new AppointmentName("Viewing"), "Viewing");
    }
}
