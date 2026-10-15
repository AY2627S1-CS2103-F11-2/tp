package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TYPE;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PhoneMatchesKeywordPredicate;
import seedu.address.model.person.Type;
import seedu.address.model.person.TypeMatchesKeywordPredicate;

/**
 * Parses input arguments and creates a new SearchCommand object
 */
public class SearchCommandParser implements Parser<SearchCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the SearchCommand
     * and returns a SearchCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public SearchCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                args, PREFIX_NAME, PREFIX_PHONE, PREFIX_TYPE);

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_NAME, PREFIX_PHONE, PREFIX_TYPE);

        if (!hasMinimumOneParam(argMultimap) || !argMultimap.getPreamble().isEmpty()
                || hasUnsupportedPrefix(args)) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCommand.MESSAGE_USAGE));
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

        return new SearchCommand(predicate);
    }

    /**
     * Returns true if the Search command contains at least one PREFIX in it's input
     * Else returns false if there is no PREFIX specified in it's input
     * Valid PREFIX for Search command : PREFIX_NAME, PREFIX_PHONE, PREFIX_TYPE
     */
    public static boolean hasMinimumOneParam(ArgumentMultimap argMultimap) {
        boolean hasNameParam = argMultimap.getValue(PREFIX_NAME).isPresent();
        boolean hasPhoneParam = argMultimap.getValue(PREFIX_PHONE).isPresent();
        boolean hasTypeParam = argMultimap.getValue(PREFIX_TYPE).isPresent();

        return hasNameParam || hasPhoneParam || hasTypeParam;
    }


    /**
     * Returns true if the given arguments contain an unsupported prefix.
     * A prefix is considered unsupported if it appears before a slash and does not
     * match any of the valid prefixes: {@code PREFIX_NAME}, {@code PREFIX_PHONE},
     * or {@code PREFIX_TYPE}.
     *
     * @param args the input arguments to check
     * @return true if an unsupported prefix is found, false otherwise
     */
    private boolean hasUnsupportedPrefix(String args) {
        String[] tokens = args.trim().split("\\s+");

        for (String token : tokens) {
            int slashIndex = token.indexOf('/');

            if (slashIndex > 0) {
                String prefix = token.substring(0, slashIndex + 1);

                if (!prefix.equals(PREFIX_NAME.toString())
                        && !prefix.equals(PREFIX_PHONE.toString())
                        && !prefix.equals(PREFIX_TYPE.toString())) {
                    return true;
                }
            }
        }

        return false;
    }
}
