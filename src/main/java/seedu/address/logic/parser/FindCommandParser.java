package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TYPE;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PhoneMatchesKeywordPredicate;
import seedu.address.model.person.Type;
import seedu.address.model.person.TypeMatchesKeywordPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                args, PREFIX_NAME, PREFIX_PHONE, PREFIX_TYPE);

        if (!hasMinimumOneParam(argMultimap) || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        Predicate<Person> predicate = null;

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
            String[] keywords = name.toString().split("\\s+");
            predicate = new NameContainsKeywordsPredicate(List.of(keywords));
        }

        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
            Predicate<Person> phonePredicate = new PhoneMatchesKeywordPredicate(phone);

            if (predicate == null) {
                predicate = phonePredicate;
            } else {
                predicate = predicate.and(phonePredicate);
            }
        }

        if (argMultimap.getValue(PREFIX_TYPE).isPresent()) {
            Type type = ParserUtil.parseType(argMultimap.getValue(PREFIX_TYPE).get());
            Predicate<Person> typePredicate = new TypeMatchesKeywordPredicate(type);

            if (predicate == null) {
                predicate = typePredicate;
            } else {
                predicate = predicate.and(typePredicate);
            }
        }

        return new FindCommand(predicate);
    }

    /**
     * Returns true if the Find command contains at least one PREFIX in it's input
     * Else returns false if there is no PREFIX specified in it's input
     * Valid PREFIX for Find command : PREFIX_NAME, PREFIX_PHONE, PREFIX_TYPE
     */
    public static boolean hasMinimumOneParam(ArgumentMultimap argMultimap) {
        boolean hasNameParam = argMultimap.getValue(PREFIX_NAME).isPresent();
        boolean hasPhoneParam = argMultimap.getValue(PREFIX_PHONE).isPresent();
        boolean hasTypeParam = argMultimap.getValue(PREFIX_TYPE).isPresent();

        return hasNameParam || hasPhoneParam || hasTypeParam;
    }
}
