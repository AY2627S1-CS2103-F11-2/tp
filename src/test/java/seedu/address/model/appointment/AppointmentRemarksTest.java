package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AppointmentRemarksTest {
    @Test
    public void constructor_validRemarks_preservesValue() {
        AppointmentRemarks remarks = new AppointmentRemarks("Bring keys  and documents");
        assertEquals("Bring keys  and documents", remarks.getValue());
        assertEquals("Bring keys  and documents", remarks.toString());
    }

    @Test
    public void constructor_emptyRemarks_isAllowed() {
        assertEquals("", new AppointmentRemarks("").getValue());
    }

    @Test
    public void constructor_whitespaceAndUnicodeRemarks_preserveValue() {
        String value = "  Bring keys\n and documents  ";
        assertEquals(value, new AppointmentRemarks(value).getValue());
    }

    @Test
    public void constructor_nullRemarks_rejects() {
        assertThrows(NullPointerException.class, () -> new AppointmentRemarks(null));
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        assertEquals(new AppointmentRemarks("Bring keys"), new AppointmentRemarks("Bring keys"));
        assertEquals(new AppointmentRemarks("Bring keys").hashCode(),
                new AppointmentRemarks("Bring keys").hashCode());
        assertNotEquals(new AppointmentRemarks("Bring keys"), new AppointmentRemarks("Bring documents"));
        assertNotEquals(new AppointmentRemarks("Bring keys"), null);
        assertNotEquals(new AppointmentRemarks("Bring keys"), "Bring keys");
    }
}
