package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PhoneMatchesKeywordPredicateTest {
    @Test
    public void equals() {
        Phone phoneOne = new Phone("98765432");
        Phone phoneTwo = new Phone("12345678");

        PhoneMatchesKeywordPredicate predicateOne = new PhoneMatchesKeywordPredicate(phoneOne);
        PhoneMatchesKeywordPredicate predicateTwo = new PhoneMatchesKeywordPredicate(phoneTwo);

        // same object -> returns true
        assertTrue(predicateOne.equals(predicateOne));

        // same values -> returns true
        PhoneMatchesKeywordPredicate predicateOneCopy = new PhoneMatchesKeywordPredicate(phoneOne);
        assertTrue(predicateOne.equals(predicateOneCopy));

        // different types -> returns false
        assertFalse(predicateOne.equals("76541239"));

        // null -> returns false
        assertFalse(predicateOne.equals(null));

        // different phone number -> returns false
        assertFalse(predicateOne.equals(predicateTwo));
    }

    @Test
    public void test_phoneMatchesKeyword_returnsTrue() {
        PhoneMatchesKeywordPredicate predicate = new PhoneMatchesKeywordPredicate(new Phone("12345678"));
        assertTrue(predicate.test(new PersonBuilder().withPhone("12345678").build()));
    }

    @Test
    public void test_phoneDoesNotMatchesKeyword_returnsFalse() {
        PhoneMatchesKeywordPredicate predicate = new PhoneMatchesKeywordPredicate(new Phone("12345678"));
        assertFalse(predicate.test(new PersonBuilder().withPhone("98765432").build()));
    }

    @Test
    public void toStringMethod() {
        Phone phone = new Phone("98765432");
        PhoneMatchesKeywordPredicate predicate = new PhoneMatchesKeywordPredicate(phone);

        String expected = PhoneMatchesKeywordPredicate.class.getCanonicalName() + "{phone keywords=" + phone +"}";
        assertEquals(expected, predicate.toString());
    }
}
