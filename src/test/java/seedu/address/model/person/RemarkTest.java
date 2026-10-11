package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_anyNonNullValue_preservesValue() {
        assertEquals("", new Remark("").value);
        assertEquals(" ", new Remark(" ").value);
        String multilineRemark = "Prefers evening calls.\nBudget: $500,000";
        assertEquals(multilineRemark, new Remark(multilineRemark).value);
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Prefers evening calls");

        // same values -> returns true
        assertTrue(remark.equals(new Remark("Prefers evening calls")));

        // same object -> returns true
        assertTrue(remark.equals(remark));

        // null -> returns false
        assertFalse(remark.equals(null));

        // different types -> returns false
        assertFalse(remark.equals(5.0f));

        // different values -> returns false
        assertFalse(remark.equals(new Remark("Prefers morning calls")));
    }

    @Test
    public void hashCode_sameValues_returnsSameHashCode() {
        Remark remark = new Remark("Prefers evening calls");
        Remark anotherRemark = new Remark("Prefers evening calls");

        assertEquals(remark.hashCode(), anotherRemark.hashCode());
    }

    @Test
    public void toStringMethod() {
        Remark remark = new Remark("Prefers evening calls");

        assertEquals("Prefers evening calls", remark.toString());
    }
}
