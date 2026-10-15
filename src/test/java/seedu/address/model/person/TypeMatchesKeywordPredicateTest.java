package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TypeMatchesKeywordPredicateTest {
    @Test
    public void equals() {
        Type typeOne = new Type("buyer");
        Type typeTwo = new Type("seller");

        TypeMatchesKeywordPredicate predicateOne = new TypeMatchesKeywordPredicate(typeOne);
        TypeMatchesKeywordPredicate predicateTwo = new TypeMatchesKeywordPredicate(typeTwo);

        // same object -> returns true
        assertTrue(predicateOne.equals(predicateOne));

        // same values -> returns true
        TypeMatchesKeywordPredicate predicateOneCopy = new TypeMatchesKeywordPredicate(typeOne);
        assertTrue(predicateOne.equals(predicateOneCopy));

        // different types -> returns false
        assertFalse(predicateOne.equals(1));

        // null -> returns false
        assertFalse(predicateOne.equals(null));

        // different person -> returns false
        assertFalse(predicateOne.equals(predicateTwo));
    }

    @Test
    public void test_containsCorrectType_returnsTrue() {
        Type typeOne = new Type("buyer");
        Type typeTwo = new Type("seller");
        Type typeThree = new Type("tenant");
        Type typeFour = new Type("landlord");

        TypeMatchesKeywordPredicate predicateOne = new TypeMatchesKeywordPredicate(typeOne);
        assertTrue(predicateOne.test(new PersonBuilder().withType("buyer").build()));

        TypeMatchesKeywordPredicate predicateTwo = new TypeMatchesKeywordPredicate(typeTwo);
        assertTrue(predicateTwo.test(new PersonBuilder().withType("seller").build()));

        TypeMatchesKeywordPredicate predicateThree = new TypeMatchesKeywordPredicate(typeThree);
        assertTrue(predicateThree.test(new PersonBuilder().withType("tenant").build()));

        TypeMatchesKeywordPredicate predicateFour = new TypeMatchesKeywordPredicate(typeFour);
        assertTrue(predicateFour.test(new PersonBuilder().withType("landlord").build()));
    }

    @Test
    public void test_doesNotContainCorrectType_returnsFalse() {
        Type validTypeOne = new Type("buyer");
        Type validTypeTwo = new Type("seller");

        TypeMatchesKeywordPredicate predicateOne = new TypeMatchesKeywordPredicate(validTypeOne);
        assertFalse(predicateOne.test(new PersonBuilder().withType("seller").build()));

        TypeMatchesKeywordPredicate predicateTwo = new TypeMatchesKeywordPredicate(validTypeOne);
        assertFalse(predicateTwo.test(new PersonBuilder().withType("landlord").build()));

        TypeMatchesKeywordPredicate predicateThree = new TypeMatchesKeywordPredicate(validTypeTwo);
        assertFalse(predicateThree.test(new PersonBuilder().withType("buyer").build()));

        TypeMatchesKeywordPredicate predicateFour = new TypeMatchesKeywordPredicate(validTypeTwo);
        assertFalse(predicateFour.test(new PersonBuilder().withType("tenant").build()));
    }

    @Test
    public void toStringMethod() {
        Type type = new Type("landlord");
        TypeMatchesKeywordPredicate predicate = new TypeMatchesKeywordPredicate(type);

        String expected = TypeMatchesKeywordPredicate.class.getCanonicalName() + "{type keywords=" + type + "}";
        assertEquals(expected, predicate.toString());
    }
}
