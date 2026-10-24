package seedu.address.ui;

import java.time.format.DateTimeFormatter;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.appointment.Appointment;

/** Appointment list with displayed references and a visible empty state. */
public class AppointmentListPanel extends UiPart<Region> {
    private static final String FXML = "AppointmentListPanel.fxml";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    @FXML
    private ListView<Appointment> appointmentListView;

    /** Creates a panel backed by the model's live appointment results. */
    public AppointmentListPanel(ObservableList<Appointment> appointments) {
        super(FXML);
        appointmentListView.setItems(appointments);
        Label emptyState = new Label("No appointments found.");
        emptyState.getStyleClass().add("label-bright");
        appointmentListView.setPlaceholder(emptyState);
        appointmentListView.setCellFactory(list -> new AppointmentListCell());
    }

    /** Re-renders references when cells move, including after deletion or filtering. */
    private static class AppointmentListCell extends ListCell<Appointment> {
        @Override
        protected void updateItem(Appointment appointment, boolean empty) {
            super.updateItem(appointment, empty);
            render();
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            render();
        }

        private void render() {
            Appointment appointment = getItem();
            setText(null);
            if (isEmpty() || appointment == null || getIndex() < 0) {
                setGraphic(null);
                return;
            }
            Label title = label("A" + (getIndex() + 1) + ". " + appointment.getName());
            title.getStyleClass().remove("cell_small_label");
            title.getStyleClass().add("cell_big_label");
            VBox card = new VBox(6, title, label("Client: " + appointment.getClient().getName().fullName),
                    label("Start: " + appointment.getStart().format(DATE_TIME) + " (Singapore)"),
                    label("End: " + appointment.getEnd().format(DATE_TIME) + " (Singapore)"),
                    label("Location: " + appointment.getLocation()));
            card.setStyle("-fx-padding: 10;");
            setGraphic(card);
        }

        private Label label(String text) {
            Label label = new Label(text);
            label.setWrapText(true);
            label.getStyleClass().add("cell_small_label");
            label.maxWidthProperty().bind(widthProperty().subtract(35));
            return label;
        }
    }
}
