package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/** Displays the current details of the client selected in the list. */
public class PersonDetailsPanel extends UiPart<Region> {
    private static final String FXML = "PersonDetailsPanel.fxml";

    @FXML
    private Label emptyState;
    @FXML
    private VBox details;
    @FXML
    private Label clientName;
    @FXML
    private Label clientId;
    @FXML
    private Label clientType;
    @FXML
    private Label clientPhone;
    @FXML
    private Label clientEmail;
    @FXML
    private Label clientAddress;
    @FXML
    private FlowPane tags;

    /** Creates an empty details panel until a client is selected. */
    public PersonDetailsPanel() {
        super(FXML);
        showPerson(null);
    }

    /** Shows the selected client's latest record, or an empty state when selection is cleared. */
    public void showPerson(Person person) {
        boolean hasSelection = person != null;
        emptyState.setVisible(!hasSelection);
        emptyState.setManaged(!hasSelection);
        details.setVisible(hasSelection);
        details.setManaged(hasSelection);
        tags.getChildren().clear();
        clientName.setText(hasSelection ? person.getName().fullName : "");
        clientId.setText(hasSelection ? "Client ID: "
                + person.getClientId().map(Object::toString).orElse("Unassigned") : "");
        clientType.setText(hasSelection ? "Type: " + person.getType().clientType : "");
        clientPhone.setText(hasSelection ? "Phone: " + person.getPhone().value : "");
        clientEmail.setText(hasSelection ? "Email: " + person.getEmail().getDisplayValue() : "");
        clientAddress.setText(hasSelection ? "Address: " + person.getAddress().value : "");
        if (hasSelection) {
            person.getTags().stream().sorted(Comparator.comparing(tag -> tag.tagName))
                    .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        }
    }
}
