package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.util.SampleDataUtil;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_emptyRemark_acceptsEmptyString() {
        assertEquals("", new Remark("").value);
    }

    @Test
    public void getSamplePersons_emptyRemarks_initializesSuccessfully() {
        Person[] persons = SampleDataUtil.getSamplePersons();
        assertTrue(persons.length > 0);
        for (Person person : persons) {
            assertEquals(new Remark(""), person.getRemark());
        }
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes swimming");
        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("Likes swimming")));
        assertEquals(remark.hashCode(), new Remark("Likes swimming").hashCode());
        assertFalse(remark.equals(new Remark("Likes reading")));
        assertFalse(remark.equals(new Address("Likes swimming")));
        assertFalse(remark.equals(null));
    }
}
