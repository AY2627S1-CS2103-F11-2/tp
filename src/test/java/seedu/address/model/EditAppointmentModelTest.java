package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalAppointments.SIGNING;
import static seedu.address.testutil.TypicalAppointments.VIEWING;
import static seedu.address.testutil.TypicalAppointments.getAppointmentAddressBook;
import static seedu.address.testutil.TypicalPersons.CARL;

import org.junit.jupiter.api.Test;

public class EditAppointmentModelTest {
    @Test
    public void setAppointment_invalidReplacement_preservesDataAndHistory() {
        ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        assertThrows(IllegalArgumentException.class, () -> model.setAppointment(VIEWING, SIGNING));
        assertThrows(IllegalArgumentException.class, () -> model.setAppointment(VIEWING, VIEWING.withClient(CARL)));
        assertThrows(IllegalArgumentException.class, () -> model.setAppointment(VIEWING.withClient(CARL), VIEWING));
        assertThrows(NullPointerException.class, () -> model.setAppointment(null, VIEWING));
        assertThrows(NullPointerException.class, () -> model.setAppointment(VIEWING, null));
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void setAppointment_sameValue_doesNotRecordHistory() {
        ModelManager model = new ModelManager(getAppointmentAddressBook(), new UserPrefs());
        model.setAppointment(VIEWING, VIEWING);
        assertEquals(getAppointmentAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }
}
