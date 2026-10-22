package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final ReadOnlyObjectWrapper<Person> selectedPerson = new ReadOnlyObjectWrapper<>();
    private Person selectionBeforeChange;
    private boolean updatingItems;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        // Capture identity before ListView moves its selection while processing changed rows.
        personList.addListener((ListChangeListener<Person>) change -> {
            updatingItems = true;
            selectionBeforeChange = selectedPerson.get();
        });
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            if (!updatingItems) {
                selectedPerson.set(findCurrentPerson(selected));
            }
        });
        // This listener runs after ListView's own list listeners have finished their bookkeeping.
        personList.addListener((ListChangeListener<Person>) change -> {
            try {
                selectPerson(selectionBeforeChange);
            } finally {
                updatingItems = false;
            }
        });
    }

    /** Returns the currently selected client, which may be absent. */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return selectedPerson.getReadOnlyProperty();
    }

    /** Selects and reveals a client after a successfully saved command. */
    public void selectPerson(Person person) {
        Person currentPerson = findCurrentPerson(person);
        boolean wasUpdatingItems = updatingItems;
        updatingItems = true;
        try {
            selectedPerson.set(currentPerson);
            if (currentPerson == null) {
                personListView.getSelectionModel().clearSelection();
            } else {
                personListView.getSelectionModel().select(currentPerson);
                personListView.scrollTo(currentPerson);
            }
        } finally {
            updatingItems = wasUpdatingItems;
        }
    }

    private Person findCurrentPerson(Person person) {
        if (person == null) {
            return null;
        }
        return personListView.getItems().stream()
                .filter(current -> person.getClientId().isPresent()
                        ? current.getClientId().equals(person.getClientId()) : current.equals(person))
                .findFirst().orElse(null);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);
            render();
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            render();
        }

        private void render() {
            Person person = getItem();

            if (isEmpty() || person == null || getIndex() < 0) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
