package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("peter*")); // punctuation is allowed with a letter or digit
        assertTrue(Name.isValidName("D'Arcy-Smith, Jr."));
        assertTrue(Name.isValidName("张伟 José 李")); // Unicode letters
        assertTrue(Name.isValidName("Jose\u0301")); // combining mark
        assertTrue(Name.isValidName("١٢٣")); // Unicode digits
        assertTrue(Name.isValidName("Alex 😀")); // printable symbols
    }

    @Test
    public void isValidName_nonPrintableCharacters_returnsFalse() {
        String[] invalidNames = {"Alex\tTan", "Alex\nTan", "Alex\rTan", "Alex\u0000Tan", "Alex\u007fTan",
            "Alex\u200bTan", "Alex\u2028Tan", "Alex\u2029Tan", "Alex\u0378Tan", "Alex\ud800Tan",
            "Alex\udc00Tan"};
        for (String invalidName : invalidNames) {
            assertFalse(Name.isValidName(invalidName));
            assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
        }
    }

    @Test
    public void isValidName_normalizedLengthBoundaries() {
        assertTrue(Name.isValidName("A"));
        assertTrue(Name.isValidName("A".repeat(100)));
        assertFalse(Name.isValidName("A".repeat(101)));
        assertTrue(Name.isValidName("  " + "A".repeat(100) + "  "));
        assertTrue(Name.isValidName("A".repeat(98) + "   B"));
        assertFalse(Name.isValidName("A".repeat(99) + "   B"));

        // A supplementary Unicode letter occupies two UTF-16 units, but is one character.
        String supplementaryLetter = "\ud801\udc00";
        assertTrue(Name.isValidName(supplementaryLetter.repeat(100)));
        assertFalse(Name.isValidName(supplementaryLetter.repeat(101)));
        assertThrows(IllegalArgumentException.class, () -> new Name(supplementaryLetter.repeat(101)));
    }

    @Test
    public void constructor_spaces_normalizesAndPreservesCase() {
        Name name = new Name("  José   D'Arcy-Smith  ");
        assertEquals("José D'Arcy-Smith", name.fullName);
        assertEquals("José D'Arcy-Smith", name.toString());
        assertEquals(name, new Name(name.fullName));
    }

    @Test
    public void constructor_unicodeSpaceSeparators_normalizes() {
        Name name = new Name("\u00a0Alex\u2002\u2003Tan\u3000");
        assertEquals("Alex Tan", name.fullName);
        assertFalse(Name.isValidName("\u00a0\u2002\u3000"));
        assertFalse(Name.isValidName(" \u0301! ")); // marks and punctuation without a letter or digit
    }

    @Test
    public void equalsAndHashCode_normalizedNames_match() {
        Name name = new Name("Alex Tan");
        Name spacedName = new Name("  Alex   Tan ");
        assertEquals(name, spacedName);
        assertEquals(name.hashCode(), spacedName.hashCode());
        assertFalse(name.equals(new Name("alex tan")));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
