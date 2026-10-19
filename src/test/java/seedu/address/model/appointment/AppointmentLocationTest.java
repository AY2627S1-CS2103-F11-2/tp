package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AppointmentLocationTest {
    @Test
    public void constructor_validLocation_trimsEdgesPreservesInternalWhitespace() {
        AppointmentLocation location = new AppointmentLocation("  NUS  COM3 #02-01  ");
        assertEquals("NUS  COM3 #02-01", location.getValue());
        assertEquals("NUS  COM3 #02-01", location.toString());
    }

    @Test
    public void constructor_blankLocation_rejects() {
        for (String value : new String[]{"", " ", "\t\n", "\u2003"}) {
            assertThrows(IllegalArgumentException.class, () -> new AppointmentLocation(value));
        }
        assertThrows(NullPointerException.class, () -> new AppointmentLocation(null));
    }

    @Test
    public void constructor_nonblankLocation_preservesPunctuationAndWhitespace() {
        for (String value : new String[]{"#02-01", "NUS\nCOM3", "Block 123, Unit #04-05"}) {
            assertEquals(value, new AppointmentLocation(value).getValue());
        }
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        assertEquals(new AppointmentLocation("NUS"), new AppointmentLocation(" NUS "));
        assertEquals(new AppointmentLocation("NUS").hashCode(), new AppointmentLocation("NUS").hashCode());
        assertNotEquals(new AppointmentLocation("NUS"), new AppointmentLocation("NTU"));
        assertNotEquals(new AppointmentLocation("NUS"), null);
        assertNotEquals(new AppointmentLocation("NUS"), "NUS");
    }
}
