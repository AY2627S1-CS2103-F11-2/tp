package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents the Type of client the Person is to the user.
 * Guarantees : Immutable, is valid as declared in {@link #isValidType(String)}
 */
public class Type {
    public static final String MESSAGE_CONSTRAINTS =
            "Type should contain only 1 of 'buyer', 'seller', 'landlord' or 'tenant' in lowercase";
    public static final String VALIDATION_REGEX = "^(buyer|seller|tenant|landlord)$";
    public final String clientType;

    /**
     * Constructs a {@code Type}
     *
     * @param type, A valid type (ie: 'buyer', 'seller', 'landlord' or 'seller')
     */
    public Type(String type) {
        requireNonNull(type);
        checkArgument(isValidType(type), MESSAGE_CONSTRAINTS);
        clientType = type;
    }

    /**
     * Returns true if the given string is a valid type
     * @param type, User inputted type
     * @return True if user inputted type is valid, false if not
     */
    public static Boolean isValidType(String type) {
        return type.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return clientType;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Type otherType)) {
            return false;
        }

        return clientType.equals(otherType.clientType);
    }

    @Override
    public int hashCode() {
        return clientType.hashCode();
    }
}
