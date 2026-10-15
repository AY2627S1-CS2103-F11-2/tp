package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddAppointmentCommand;
import seedu.address.logic.commands.AppointmentsCommand;
import seedu.address.logic.commands.DeleteAppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class AppointmentParsersTest {
    private final AppointmentsCommandParser viewParser = new AppointmentsCommandParser();
    private final DeleteAppointmentCommandParser deleteParser = new DeleteAppointmentCommandParser();
    private final AddAppointmentCommandParser addParser = new AddAppointmentCommandParser();

    @Test
    public void appointments_validArguments_parsesAnyOrderWhitespaceAndEmpty() throws Exception {
        assertEquals(new AppointmentsCommand(null, null), viewParser.parse(""));
        assertEquals(new AppointmentsCommand(Index.fromOneBased(2), LocalDate.of(2026, 10, 15)),
                viewParser.parse("\td/ 15102026 \tc/ C2  "));
        assertEquals(new AppointmentsCommand(null, LocalDate.of(2024, 2, 29)), viewParser.parse("d/29022024"));
    }

    @Test
    public void appointments_invalidArguments_rejectsWithPreciseErrors() {
        for (String args : new String[]{" c/C0", " c/1", " c/c1", " c/C01", " c/C-1", " c/C1.5",
            " c/C2147483648", " c/C99999999999999999999", " c/C١"}) {
            assertError(viewParser, args, AppointmentsCommandParser.MESSAGE_CLIENT_INDEX);
        }
        assertError(viewParser, " c/", AppointmentsCommandParser.MESSAGE_EMPTY_CLIENT);
        assertError(viewParser, " d/", ParserUtil.MESSAGE_EMPTY_DATE);
        for (String args : new String[]{" d/30022026", " d/29022025", " d/15132026", " d/15-10-2026",
            " d/2026/10/15", " d/tomorrow", " d/1510202"}) {
            assertError(viewParser, args, ParserUtil.MESSAGE_INVALID_DATE);
        }
        for (String args : new String[]{" c/C1 c/C1", " d/15102026 d/15102026"}) {
            assertError(viewParser, args, AppointmentsCommandParser.MESSAGE_DUPLICATE);
        }
        for (String args : new String[]{" hello", " x/test", " c/C1 x/test", " d/15102026 extra"}) {
            assertError(viewParser, args, AppointmentsCommandParser.MESSAGE_FORMAT);
        }
    }

    @Test
    public void deleteAppointment_validConfirmation_parses() throws Exception {
        for (String args : new String[]{" A1 yes", " A1 confirm", "\tA1 \tconfirm  "}) {
            assertEquals(new DeleteAppointmentCommand(Index.fromOneBased(1)), deleteParser.parse(args));
        }
    }

    @Test
    public void deleteAppointment_missingExtraOrInvalidArguments_rejects() {
        for (String args : new String[]{"", " A1 yes extra", " A1 A2 yes"}) {
            assertError(deleteParser, args, DeleteAppointmentCommandParser.MESSAGE_FORMAT);
        }
        for (String args : new String[]{" A1", " A1 no", " A1 YES", " A1 yes/confirm"}) {
            assertError(deleteParser, args, ParserUtil.MESSAGE_MISSING_CONFIRMATION);
        }
        for (String args : new String[]{" 1 yes", " a1 yes", " A0 yes", " A-1 yes", " A1.5 yes",
            " A2147483648 yes", " A99999999999999999999 yes"}) {
            assertError(deleteParser, args, ParserUtil.MESSAGE_INVALID_APPOINTMENT_INDEX);
        }
    }

    @Test
    public void addAppointment_invalidFieldsAndTimes_rejects() {
        String valid = " n/Viewing c/C1 loc/NUS s/20261015-1000 e/20261015-1100";
        for (String args : new String[]{"", valid + " n/Viewing", valid + " x/extra",
            valid.replace("n/Viewing", "n/"), valid.replace("n/Viewing", "n/Viewing1"),
            valid.replace("loc/NUS", "loc/"), valid.replace("loc/NUS", "loc/NUS#1"),
            valid.replace("c/C1", "c/C0"), valid.replace("s/20261015-1000", "s/20260230-1000"),
            valid.replace("s/20261015-1000", "s/20261015-2400"),
            valid.replace("e/20261015-1100", "e/20261015-1000"),
            valid.replace("e/20261015-1100", "e/20261014-1100")}) {
            assertThrows(ParseException.class, () -> addParser.parse(args), args);
        }
    }

    @Test
    public void addressBookParser_routesAllAppointmentCommands() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertInstanceOf(AppointmentsCommand.class, parser.parseCommand("appointments"));
        assertInstanceOf(DeleteAppointmentCommand.class, parser.parseCommand("deleteappt A1 confirm"));
        assertInstanceOf(AddAppointmentCommand.class, parser.parseCommand(
                "appointment n/Viewing c/C1 loc/NUS s/20261015-1000 e/20261015-1100"));
    }

    private void assertError(Parser<?> parser, String args, String expected) {
        assertEquals(expected, assertThrows(ParseException.class, () -> parser.parse(args), args).getMessage());
    }
}
