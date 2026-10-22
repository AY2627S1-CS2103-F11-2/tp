package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ClientIdTest {

    @Test
    public void constructor_rejectsInvalidNumbersAndOverflowBoundary() {
        assertThrows(IllegalArgumentException.class, () -> new ClientId(0));
        assertThrows(IllegalArgumentException.class, () -> new ClientId(-1));
        assertThrows(IllegalArgumentException.class, () -> new ClientId(Long.MAX_VALUE));
    }

    @Test
    public void valueEqualityAndDisplay_usePositiveNumericComponent() {
        ClientId id = new ClientId(12);
        assertEquals(12, id.getNumber());
        assertEquals("C12", id.toString());
        assertEquals(id, new ClientId(12));
        assertEquals(id.hashCode(), new ClientId(12).hashCode());
        assertNotEquals(id, new ClientId(13));
        assertNotEquals(id, "C12");
    }
}
