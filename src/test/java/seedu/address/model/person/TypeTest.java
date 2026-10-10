package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TypeTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Type(null));
    }

    @Test
    public void constructor_invalidType_throwsIllegalArgumentException() {
        String invalidType = "";
        assertThrows(IllegalArgumentException.class, () -> new Type(invalidType));
    }

    @Test
    public void isValidType() {
        // null type
        assertThrows(NullPointerException.class, () -> Type.isValidType(null));

        // invalid types
        assertFalse(Type.isValidType("")); // empty string
        assertFalse(Type.isValidType(" ")); // spaces only
        assertFalse(Type.isValidType("Buyer")); // uppercase letter
        assertFalse(Type.isValidType("BUYER")); // uppercase letters
        assertFalse(Type.isValidType(" buyer")); // leading space
        assertFalse(Type.isValidType("buyer ")); // trailing space
        assertFalse(Type.isValidType("buyer seller")); // multiple types
        assertFalse(Type.isValidType("owner")); // unsupported type
        assertFalse(Type.isValidType("landlord/tenant")); // combined types

        // valid types
        assertTrue(Type.isValidType("buyer"));
        assertTrue(Type.isValidType("seller"));
        assertTrue(Type.isValidType("landlord"));
        assertTrue(Type.isValidType("tenant"));
    }

    @Test
    public void toStringMethod() {
        Type type = new Type("buyer");

        // returns the client type as a string
        assertEquals("buyer", type.toString());
    }

    @Test
    public void equals() {
        Type type = new Type("buyer");

        // same values -> returns true
        assertTrue(type.equals(new Type("buyer")));

        // same object -> returns true
        assertTrue(type.equals(type));

        // null -> returns false
        assertFalse(type.equals(null));

        // different types -> returns false
        assertFalse(type.equals(5.0f));

        // different values -> returns false
        assertFalse(type.equals(new Type("seller")));
    }

    @Test
    public void hashCode_sameValues_returnsSameHashCode() {
        Type type = new Type("buyer");
        Type anotherType = new Type("buyer");

        assertEquals(type.hashCode(), anotherType.hashCode());
    }
}
