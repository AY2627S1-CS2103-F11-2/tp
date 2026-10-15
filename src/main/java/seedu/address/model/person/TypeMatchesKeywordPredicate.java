package seedu.address.model.person;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Type} matches the Type param in Search command.
 */
public class TypeMatchesKeywordPredicate implements Predicate<Person> {
    private final Type type;

    public TypeMatchesKeywordPredicate(Type type) {
        this.type = type;
    }

    @Override
    public boolean test(Person person) {
        return person.getType().equals(type);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TypeMatchesKeywordPredicate otherTypeMatchesKeywordPredicate)) {
            return false;
        }

        return type.equals(otherTypeMatchesKeywordPredicate.type);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("type keywords", type).toString();
    }
}

