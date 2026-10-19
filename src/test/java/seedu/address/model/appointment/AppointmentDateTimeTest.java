package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class AppointmentDateTimeTest {
    private static final LocalDateTime VALUE = LocalDateTime.of(2026, 10, 20, 10, 30);

    @Test
    public void constructor_validValue_preservesValue() {
        AppointmentDateTime dateTime = new AppointmentDateTime(VALUE);
        assertEquals(VALUE, dateTime.getValue());
        assertEquals(VALUE.toString(), dateTime.toString());
    }

    @Test
    public void constructor_nullValue_rejects() {
        assertThrows(NullPointerException.class, () -> new AppointmentDateTime(null));
    }

    @Test
    public void constructor_boundaryValues_preserveValue() {
        LocalDateTime earliest = LocalDateTime.MIN;
        LocalDateTime latest = LocalDateTime.MAX;
        assertEquals(earliest, new AppointmentDateTime(earliest).getValue());
        assertEquals(latest, new AppointmentDateTime(latest).getValue());
    }

    @Test
    public void compareTo_ordersChronologically() {
        AppointmentDateTime earlier = new AppointmentDateTime(VALUE.minusHours(1));
        AppointmentDateTime later = new AppointmentDateTime(VALUE.plusHours(1));
        assertEquals(0, new AppointmentDateTime(VALUE).compareTo(new AppointmentDateTime(VALUE)));
        assertEquals(-1, Integer.signum(earlier.compareTo(later)));
        assertEquals(1, Integer.signum(later.compareTo(earlier)));
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        AppointmentDateTime first = new AppointmentDateTime(VALUE);
        AppointmentDateTime second = new AppointmentDateTime(VALUE);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new AppointmentDateTime(VALUE.plusNanos(1)));
        assertNotEquals(first, null);
        assertNotEquals(first, VALUE);
    }
}
