package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;

/** A stable client identifier, independent of the displayed list position. */
public final class ClientId {

    public static final String MESSAGE_CONSTRAINTS =
            "Client ID must be a positive integer below " + Long.MAX_VALUE + ".";

    private final long number;

    /** Creates a client identifier from its positive numeric component. */
    public ClientId(long number) {
        checkArgument(isValidNumber(number), MESSAGE_CONSTRAINTS);
        this.number = number;
    }

    /** Returns whether a number can be allocated as a client identifier. */
    public static boolean isValidNumber(long number) {
        return number > 0 && number < Long.MAX_VALUE;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "C" + number;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClientId otherId && number == otherId.number;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(number);
    }
}
