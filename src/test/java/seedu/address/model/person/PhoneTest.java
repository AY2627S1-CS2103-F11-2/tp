package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 numbers
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("9312 1534")); // spaces within digits
        assertTrue(Phone.isValidPhone("+65 9312 1534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
    }

    @Test
    public void isValidPhone_digitLengthAndFormattingBoundaries() {
        for (String valid : new String[] {"101", "+1 01", "123456789012345", "+12 345 678 901 2345"}) {
            assertTrue(Phone.isValidPhone(valid), valid);
        }
        for (String invalid : new String[] {"12", "+1 2", "1234567890123456", "000", "+00 000",
            "65+91234567", "++6591234567", "9123-4567", "9123\t4567", "١٢٣", "+ 6591234567"}) {
            assertFalse(Phone.isValidPhone(invalid), invalid);
            assertThrows(IllegalArgumentException.class, () -> new Phone(invalid));
        }
    }

    @Test
    public void constructor_formattedPhone_normalizesStorageAndEquality() {
        Phone formatted = new Phone("+65  9123 4567");
        Phone digits = new Phone("6591234567");
        assertEquals("6591234567", formatted.value);
        assertEquals(digits.toString(), formatted.toString());
        assertEquals(digits, formatted);
        assertEquals(digits.hashCode(), formatted.hashCode());
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }
}
