package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;

/** Checks specification boundaries before any appointment edit reaches the model. */
public class AppointmentEditFieldsTest {
    @Test
    public void suppliedFields_validateAndNormalizeWithoutLosingAnEmptyDescription() {
        AppointmentEditFields fields = new AppointmentEditFields("  Viewing 123  ", INDEX_FIRST_PERSON,
                LocalDateTime.of(2026, 10, 18, 15, 30), "  NUS  COM3 #02-01  ", "");
        assertEquals("Viewing 123", fields.name());
        assertEquals("NUS  COM3 #02-01", fields.location());
        assertEquals("", fields.description());
        assertEquals(100, new AppointmentEditFields(null, null, null, "A".repeat(100), null).location().length());
    }

    @Test
    public void invalidFields_rejectEmptyNamesOverlongValuesAndMissingChanges() {
        assertThrows(IllegalArgumentException.class, () -> new AppointmentEditFields(null, null, null, null, null));
        for (String name : new String[]{"", " ", "Name!", "A".repeat(51)}) {
            assertThrows(IllegalArgumentException.class, ()
                    -> new AppointmentEditFields(name, null, null, null, null));
        }
        for (String location : new String[]{"", "\u2003", "A".repeat(101)}) {
            assertThrows(IllegalArgumentException.class, ()
                    -> new AppointmentEditFields(null, null, null, location, null));
        }
    }

    @Test
    public void dateTime_requiresExactFormatAndRealCalendarTime() throws Exception {
        assertEquals(LocalDateTime.of(2028, 2, 29, 23, 59),
                ParserUtil.parseAppointmentEditDateTime(" 2028-02-29 23:59 "));
        for (String value : new String[]{"2026-02-29 10:00", "2026-02-30 10:00", "2026-10-18 24:00",
            "2026-10-18 15:60", "2026-1-18 15:30", "20261018-1530", "0000-01-01 10:00", "tomorrow 2pm"}) {
            ParseException error = assertThrows(ParseException.class, ()
                    -> ParserUtil.parseAppointmentEditDateTime(value), value);
            assertEquals(AppointmentEditFields.MESSAGE_INVALID_DATE_TIME, error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAppointmentEditDateTime(null));
    }
}
