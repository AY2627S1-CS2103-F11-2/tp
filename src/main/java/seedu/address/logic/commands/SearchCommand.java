package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists all persons in the address book whose details contains any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class SearchCommand extends Command {

    public static final String COMMAND_WORD = "search";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all persons whose names contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: n/Name, p/Phone Number and y/Type\n"
            + "Example: " + COMMAND_WORD + " n/Alice p/98765432 y/seller";

    private static final Logger logger = LogsCenter.getLogger(SearchCommand.class);

    private final Predicate<Person> predicate;

    public SearchCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);

        int count = model.getFilteredPersonList().size();
        assert count >= 0
                : "Search result count cannot be negative.";
        assert count <= model.getAddressBook().getPersonList().size()
                : "Filtered clients cannot outnumber stored clients.";

        assert model.getFilteredPersonList().stream().allMatch(predicate)
                : "Every displayed client must satisfy the search predicate.";
        assert model.getFilteredPersonList().stream().distinct().count() == count
                : "Search results must not contain duplicate clients.";
        assert model.getFilteredPersonList().stream()
                .allMatch(model.getAddressBook().getPersonList()::contains)
                : "Search results must only contain stored clients.";


        if (count > 0) {
            logger.info("Search returned " + count + " result(s).");
        } else {
            logger.info("Search returned no results.");
        }

        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, count));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SearchCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
