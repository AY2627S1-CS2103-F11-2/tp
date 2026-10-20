package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ClientId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises the client selection and FXML views when a graphics environment is available. */
public class ClientDetailsUiTest {

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JavaFX UI tests require a graphics environment.");
        System.setProperty("prism.order", "sw");
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
        } catch (IllegalStateException e) {
            Platform.runLater(ready::countDown);
        }
        assertTrue(ready.await(20, TimeUnit.SECONDS), "JavaFX toolkit did not start.");
        Platform.setImplicitExit(false);
    }

    @Test
    public void details_selectedClientWithoutEmail_showsValuesAndClears() throws Exception {
        onFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel();
            Region root = panel.getRoot();
            new Scene(root);
            root.applyCss();
            assertTrue(root.lookup("#emptyState").isVisible());

            Person client = new PersonBuilder().withName("Alex Tan").withNoEmail().build()
                    .withClientId(new ClientId(12));
            panel.showPerson(client);

            assertEquals("Alex Tan", label(root, "clientName").getText());
            assertEquals("Client ID: C12", label(root, "clientId").getText());
            assertEquals("Email: Not provided", label(root, "clientEmail").getText());
            assertFalse(root.lookup("#emptyState").isVisible());

            panel.showPerson(null);

            assertTrue(root.lookup("#emptyState").isVisible());
            assertFalse(root.lookup("#details").isManaged());
            assertEquals("", label(root, "clientEmail").getText());
            assertEquals("", label(root, "clientName").getText());
        });
    }

    @Test
    public void list_replacedSelectedClientAtSameIndex_refreshesDetails() throws Exception {
        onFxThread(() -> {
            Person original = new PersonBuilder().build().withClientId(new ClientId(8));
            Person updated = new PersonBuilder(original).withPhone("91234567").build()
                    .withClientId(new ClientId(8));
            ObservableList<Person> clients = FXCollections.observableArrayList(original);
            PersonListPanel listPanel = new PersonListPanel(clients);
            PersonDetailsPanel detailsPanel = new PersonDetailsPanel();
            new Scene(detailsPanel.getRoot());
            detailsPanel.getRoot().applyCss();
            listPanel.selectedPersonProperty().addListener((observable, oldPerson, selectedPerson) ->
                    detailsPanel.showPerson(selectedPerson));
            listPanel.selectPerson(original);

            clients.set(0, updated);

            assertSame(updated, listPanel.selectedPersonProperty().get());
            assertEquals("Phone: 91234567", label(detailsPanel.getRoot(), "clientPhone").getText());
        });
    }

    @Test
    public void list_selectedClientFilteredOut_clearsSelection() throws Exception {
        onFxThread(() -> {
            Person client = new PersonBuilder().build().withClientId(new ClientId(8));
            FilteredList<Person> clients = new FilteredList<>(FXCollections.observableArrayList(client));
            PersonListPanel panel = new PersonListPanel(clients);
            panel.selectPerson(client);

            clients.setPredicate(person -> false);

            assertNull(panel.selectedPersonProperty().get());
        });
    }

    @Test
    public void list_selectedClientDeleted_clearsSelection() throws Exception {
        onFxThread(() -> {
            Person client = new PersonBuilder().build().withClientId(new ClientId(8));
            ObservableList<Person> clients = FXCollections.observableArrayList(client);
            PersonListPanel panel = new PersonListPanel(clients);
            panel.selectPerson(client);

            clients.remove(client);

            assertNull(panel.selectedPersonProperty().get());
        });
    }

    @Test
    public void card_movedToDifferentRow_retainsStableIdAndRefreshesPosition() throws Exception {
        onFxThread(() -> {
            Person client = new PersonBuilder().build().withClientId(new ClientId(20));
            Person second = new PersonBuilder().withName("Bob").build().withClientId(new ClientId(21));
            Person third = new PersonBuilder().withName("Charlie").build().withClientId(new ClientId(22));
            ObservableList<Person> clients = FXCollections.observableArrayList(client, second, third);
            PersonListPanel panel = new PersonListPanel(clients);
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
            cell.updateListView(personListView(panel));
            cell.updateIndex(0);
            assertEquals("Position C1 · Client ID C20", label((Region) cell.getGraphic(), "id").getText());

            clients.setAll(second, third, client);
            cell.updateIndex(2);

            assertSame(client, cell.getItem());
            assertEquals("Position C3 · Client ID C20", label((Region) cell.getGraphic(), "id").getText());
        });
    }

    @Test
    public void mainWindow_commandBoxAdd_savesSelectsShowsDetailsAndClearsInput() throws Exception {
        onFxThread(() -> {
            ModelManager model = new ModelManager();
            Path dataFile = testFolder.resolve("clients.json");
            StorageManager storage = storage(dataFile);
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, new LogicManager(model, storage), dataFile);
                window.fillInnerParts();
                Region root = (Region) stage.getScene().getRoot();
                root.resize(1050, 780);
                root.applyCss();
                root.layout();
                TextField command = (TextField) root.lookup("#commandTextField");

                command.setText("add n/Alex Tan p/91234567 a/123 Clementi Road y/buyer");
                command.fireEvent(new ActionEvent());

                Person selected = window.getPersonListPanel().selectedPersonProperty().get();
                assertEquals("Alex Tan", selected.getName().fullName);
                assertEquals("C1", selected.getClientId().orElseThrow().toString());
                assertEquals("", command.getText());
                assertEquals("Client ID: C1", label(root, "clientId").getText());
                assertEquals("Alex Tan", label(root, "clientName").getText());
                assertEquals("Email: Not provided", label(root, "clientEmail").getText());
                assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
                writePreviewIfRequested(root);

                String duplicateCommand = "add n/Alex Tan p/92345678 a/456 Clementi Road y/tenant";
                command.setText(duplicateCommand);
                command.fireEvent(new ActionEvent());

                assertEquals(duplicateCommand, command.getText());
                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertSame(selected, window.getPersonListPanel().selectedPersonProperty().get());
                assertEquals(1, model.getAddressBook().getPersonList().size());
                assertEquals("Alex Tan", label(root, "clientName").getText());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void mainWindow_commandBoxAddSaveFailure_retainsInputAndExistingSelection() throws Exception {
        onFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Zara").build());
            Person original = model.getAddressBook().getPersonList().get(0);
            Path dataFile = testFolder.resolve("clients.json");
            StorageManager failingStorage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw new IOException("Simulated save failure");
                }
            };
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, new LogicManager(model, failingStorage), dataFile);
                window.fillInnerParts();
                window.getPersonListPanel().selectPerson(original);
                Region root = (Region) stage.getScene().getRoot();
                root.applyCss();
                TextField command = (TextField) root.lookup("#commandTextField");
                String input = "add n/Alex Tan p/91234567 a/123 Clementi Road y/buyer";

                command.setText(input);
                command.fireEvent(new ActionEvent());

                assertEquals(input, command.getText());
                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(1, model.getAddressBook().getPersonList().size());
                assertEquals(original.getClientId(),
                        window.getPersonListPanel().selectedPersonProperty().get().getClientId());
                assertEquals("Zara", label(root, "clientName").getText());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void list_selectedClientRenamedToDifferentRow_preservesSelectionAndRefreshesDetails() throws Exception {
        onFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Zara").build());
            model.addPerson(new PersonBuilder().withName("Bob").build());
            Person original = model.getAddressBook().getPersonList().get(0);
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            PersonDetailsPanel detailsPanel = new PersonDetailsPanel();
            new Scene(detailsPanel.getRoot());
            detailsPanel.getRoot().applyCss();
            panel.selectedPersonProperty().addListener((observable, oldPerson, selectedPerson) ->
                    detailsPanel.showPerson(selectedPerson));
            panel.selectPerson(original);
            Person edited = new PersonBuilder(original).withName("Alex").build();

            model.setPerson(original, edited);

            Person selected = panel.selectedPersonProperty().get();
            assertEquals(original.getClientId(), selected.getClientId());
            assertEquals("Alex", selected.getName().fullName);
            assertSame(model.getFilteredPersonList().get(0), selected);
            assertEquals("Alex", label(detailsPanel.getRoot(), "clientName").getText());
        });
    }

    private StorageManager storage(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }

    /** Captures the actual rendered view only when a reviewer requests an output path. */
    private static void writePreviewIfRequested(Region root) throws IOException {
        String outputPath = System.getProperty("propTrack.preview.path");
        if (outputPath == null) {
            return;
        }
        root.applyCss();
        root.layout();
        WritableImage snapshot = root.getScene().snapshot(null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();
        int[] pixels = new int[width * height];
        snapshot.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        Path output = Path.of(outputPath);
        Files.createDirectories(output.toAbsolutePath().getParent());
        ImageIO.write(image, "png", output.toFile());
    }

    @SuppressWarnings("unchecked")
    private static ListView<Person> personListView(PersonListPanel panel) {
        return (ListView<Person>) panel.getRoot().lookup("#personListView");
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }

    private static void onFxThread(FxAction action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }
}
