package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PhoneMatchesKeywordPredicate;
import seedu.address.model.person.Type;
import seedu.address.model.person.TypeMatchesKeywordPredicate;
import seedu.address.testutil.PersonBuilder;

public class SearchCommandParserTest {

    private SearchCommandParser parser = new SearchCommandParser();
    private final Phone validPhone = new Phone("98765432");
    private final Type validType = new Type("buyer");

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCommand.MESSAGE_USAGE));
    }

    // Testing valid single parameters
    @Test
    public void parse_validNameParamOnly_returnsFindCommand() {
        assertParseSuccess(parser, " n/Alice",
                new SearchCommand(new NameContainsKeywordsPredicate(List.of("Alice"))));
    }

    @Test
    public void parse_validPhoneParamOnly_returnsFindCommand() {
        assertParseSuccess(parser, " p/98765432", new SearchCommand(new PhoneMatchesKeywordPredicate(validPhone)));
    }

    @Test
    public void parse_validTypeParamOnly_returnsFindCommand() {
        assertParseSuccess(parser, " y/buyer", new SearchCommand(new TypeMatchesKeywordPredicate(validType)));
    }

    // Testing all valid combination of Find parameters
    @Test
    public void parse_validNameAndPhoneParam_filtersCorrectPersons() throws ParseException {
        Person matchingPerson = new PersonBuilder()
                .withName("Alice Tan")
                .withPhone("98765432")
                .withType("seller")
                .build();

        Person wrongPhone = new PersonBuilder()
                .withName("Alice Lim")
                .withPhone("91234567")
                .withType("seller")
                .build();

        Model model = new ModelManager();
        model.addPerson(matchingPerson);
        model.addPerson(wrongPhone);

        SearchCommand command = parser.parse(" n/Alice Tan p/98765432");
        command.execute(model);

        assertEquals(List.of(matchingPerson), model.getFilteredPersonList());
    }

    @Test
    public void parse_validPhoneAndTypeParam_filtersCorrectPersons() throws ParseException {
        Person matchingPerson = new PersonBuilder()
                .withName("Alice Tan")
                .withPhone("98765432")
                .withType("seller")
                .build();

        Person wrongType = new PersonBuilder()
                .withName("Bob Tan")
                .withPhone("98765432")
                .withType("buyer")
                .build();

        Model model = new ModelManager();
        model.addPerson(matchingPerson);
        model.addPerson(wrongType);

        SearchCommand command = parser.parse(" p/98765432 y/seller");
        command.execute(model);

        assertEquals(List.of(matchingPerson), model.getFilteredPersonList());
    }

    @Test
    public void parse_validTypeAndNameParam_filtersCorrectPersons() throws ParseException {
        Person matchingPerson = new PersonBuilder()
                .withName("Alice Tan")
                .withPhone("98765432")
                .withType("seller")
                .build();

        Person wrongType = new PersonBuilder()
                .withName("Alice Lim")
                .withPhone("87654321")
                .withType("buyer")
                .build();

        Model model = new ModelManager();
        model.addPerson(matchingPerson);
        model.addPerson(wrongType);

        SearchCommand command = parser.parse(" y/seller n/Alice");
        command.execute(model);

        assertEquals(List.of(matchingPerson), model.getFilteredPersonList());
    }

    @Test
    public void parse_allValidParams_filtersCorrectPersons() throws ParseException {
        Person matchingPerson = new PersonBuilder()
                .withName("Alice Tan")
                .withPhone("98765432")
                .withType("seller")
                .build();

        Person wrongPhone = new PersonBuilder()
                .withName("Bob Tan")
                .withPhone("91234567")
                .withType("seller")
                .build();

        Person wrongType = new PersonBuilder()
                .withName("Charlie Tan")
                .withPhone("87654321")
                .withType("buyer")
                .build();

        Model model = new ModelManager();
        model.addPerson(matchingPerson);
        model.addPerson(wrongPhone);
        model.addPerson(wrongType);

        SearchCommand command = parser.parse(" n/Alice p/98765432 y/seller");
        command.execute(model);

        assertEquals(List.of(matchingPerson), model.getFilteredPersonList());
    }

    // Testing empty input in parameter
    @Test
    public void parse_emptyNameParam_throwsParseException() {
        assertParseFailure(parser, " n/", String.format(Name.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void parse_emptyPhoneParam_throwsParseException() {
        assertParseFailure(parser, " p/", String.format(Phone.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void parse_emptyTypeParam_throwsParseException() {
        assertParseFailure(parser, " y/", String.format(Type.MESSAGE_CONSTRAINTS));
    }

    // Mixture of valid and empty parameters
    @Test
    public void parse_validNameAndEmptyPhone_throwsParseException() {
        assertParseFailure(parser, " n/Alice p/", String.format(Phone.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void parse_validPhoneAndEmptyType_throwsParseException() {
        assertParseFailure(parser, " p/98765432 y/", String.format(Type.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void parse_validTypeAndEmptyName_throwsParseException() {
        assertParseFailure(parser, " y/seller n/", String.format(Name.MESSAGE_CONSTRAINTS));
    }
}
