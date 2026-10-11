package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Name must contain 1-100 characters and at least one letter or digit.";

    private static final int MAX_LENGTH = 100;
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\p{Zs}+");

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name, with spaces normalized before storage.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = normalizeSpaces(name);
    }

    /**
     * Returns true if a name contains 1-100 printable Unicode characters after space normalization,
     * including at least one letter or digit.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);
        if (!test.codePoints().allMatch(Name::isPrintable)) {
            return false;
        }
        String normalizedName = normalizeSpaces(test);
        int length = normalizedName.codePointCount(0, normalizedName.length());
        return length >= 1 && length <= MAX_LENGTH
                && normalizedName.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    /**
     * Trims and collapses Unicode space separators while preserving all other characters and letter case.
     */
    private static String normalizeSpaces(String name) {
        return SPACE_PATTERN.matcher(name).replaceAll(" ").trim();
    }

    /**
     * Returns true if a code point represents a printable character.
     */
    private static boolean isPrintable(int codePoint) {
        return switch (Character.getType(codePoint)) {
            case Character.CONTROL, Character.FORMAT, Character.SURROGATE, Character.UNASSIGNED,
                    Character.LINE_SEPARATOR, Character.PARAGRAPH_SEPARATOR -> false;
            default -> true;
        };
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
