package seedu.address.model.person;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Phone} matches the phone param in Find command.
 */
public class PhoneMatchesKeywordPredicate implements Predicate<Person> {
    private final Phone phone;

    public PhoneMatchesKeywordPredicate(Phone phone) {
        this.phone = phone;
    }

    @Override
    public boolean test(Person person) {
        return person.getPhone().equals(phone);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PhoneMatchesKeywordPredicate otherPhoneMatchesKeywordPredicate)) {
            return false;
        }

        return phone.equals(otherPhoneMatchesKeywordPredicate.phone);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("phone keywords", phone).toString();
    }
}


