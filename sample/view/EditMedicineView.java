package sample.view;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import sample.model.Medicine;
public class EditMedicineView {
    private final Stage stage;
    private final TextField nameField;
    private final TextField priceField;
    private final TextField quantityField;
    private final Button saveButton;
    private final Button cancelButton;
    // ---------------- two users editing at the same time (demo) ----------------
    private final RadioButton syncOnRadio =
            new RadioButton("With synchronization");
    private final RadioButton syncOffRadio =
            new RadioButton("Without synchronization");
    private final ComboBox<Medicine> userAMedicineBox =
            new ComboBox<>();
    private final TextField userAQuantityField =
            new TextField();
    private final ComboBox<Medicine> userBMedicineBox =
            new ComboBox<>();
    private final TextField userBQuantityField =
            new TextField();
    private final Button simulateButton =
            new Button("👥  Simulate 2 users");
    private final Label simulationResult =
            new Label();
    // the normal window (same as before)
    public EditMedicineView(Medicine medicine) {
        this(medicine, FXCollections.observableArrayList());
    }
    // 'medicines' fills the two user combo boxes of the simulation section
    public EditMedicineView(Medicine medicine, ObservableList<Medicine> medicines) {
        stage = new Stage();
        stage.setTitle("Edit Medicine");
        stage.initModality(Modality.APPLICATION_MODAL);
        Label title = new Label("Edit Medicine");
        Label nameLabel = new Label("Medicine Name");
        Label priceLabel = new Label("Price");
        Label quantityLabel = new Label("Quantity");
        nameField = new TextField();
        priceField = new TextField();
        quantityField = new TextField();
        nameField.setText(medicine.getName());
        priceField.setText(String.valueOf(medicine.getPrice()));
        quantityField.setText(String.valueOf(medicine.getQuantity()));
        saveButton = new Button("Save Changes");
        cancelButton = new Button("Cancel");
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(priceLabel, 0, 1);
        form.add(priceField, 1, 1);
        form.add(quantityLabel, 0, 2);
        form.add(quantityField, 1, 2);
        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(
                saveButton,
                cancelButton
        );
        VBox simulationBox =
                createSimulationSection(medicines);
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getChildren().addAll(
                title,
                form,
                buttons,
                new Separator(),
                simulationBox
        );
        Scene scene = new Scene(root, 620, 700);
        stage.setScene(scene);
    }
    // =========================================================
    // SIMULATION SECTION
    // =========================================================
    private VBox createSimulationSection(ObservableList<Medicine> medicines) {
        Label simulationTitle =
                new Label("Two users edit two DIFFERENT medicines at the same time");
        ToggleGroup modeGroup =
                new ToggleGroup();
        syncOnRadio.setToggleGroup(modeGroup);
        syncOffRadio.setToggleGroup(modeGroup);
        syncOnRadio.setSelected(true);
        HBox modeRow =
                new HBox(20,
                        new Label("Mode:"),
                        syncOnRadio,
                        syncOffRadio
                );
        modeRow.setAlignment(Pos.CENTER_LEFT);
        setupUserBox(userAMedicineBox, userAQuantityField, medicines);
        setupUserBox(userBMedicineBox, userBQuantityField, medicines);
        Label userALabel = new Label("User A");
        Label userBLabel = new Label("User B");
        userALabel.setMinWidth(60);
        userBLabel.setMinWidth(60);
        HBox userARow =
                new HBox(15, userALabel, userAMedicineBox, userAQuantityField);
        HBox userBRow =
                new HBox(15, userBLabel, userBMedicineBox, userBQuantityField);
        userARow.setAlignment(Pos.CENTER_LEFT);
        userBRow.setAlignment(Pos.CENTER_LEFT);
        simulateButton.setPrefHeight(40);
        simulationResult.setWrapText(true);
        return new VBox(10,
                simulationTitle,
                modeRow,
                userARow,
                userBRow,
                simulateButton,
                simulationResult
        );
    }
    private void setupUserBox(
            ComboBox<Medicine> box,
            TextField quantityField,
            ObservableList<Medicine> medicines) {
        box.setItems(medicines);
        box.setPromptText("Select medicine");
        box.setPrefWidth(300);
        box.setPrefHeight(40);
        box.setConverter(new StringConverter<Medicine>() {
            @Override
            public String toString(Medicine medicine) {
                if (medicine == null) {
                    return "";
                }
                return medicine.getName()
                        + "  (stock: "
                        + medicine.getQuantity()
                        + ")";
            }
            @Override
            public Medicine fromString(String string) {
                return null;
            }
        });
        quantityField.setPromptText("New stock");
        quantityField.setPrefWidth(110);
        quantityField.setPrefHeight(40);
    }
    public Stage getStage() {
        return stage;
    }
    public TextField getNameField() {
        return nameField;
    }
    public TextField getPriceField() {
        return priceField;
    }
    public TextField getQuantityField() {
        return quantityField;
    }
    public Button getSaveButton() {
        return saveButton;
    }
    public Button getCancelButton() {
        return cancelButton;
    }
    // ---------------- simulation getters ----------------
    public Button getSimulateButton() {
        return simulateButton;
    }
    public ComboBox<Medicine> getUserAMedicineBox() {
        return userAMedicineBox;
    }
    public TextField getUserAQuantityField() {
        return userAQuantityField;
    }
    public ComboBox<Medicine> getUserBMedicineBox() {
        return userBMedicineBox;
    }
    public TextField getUserBQuantityField() {
        return userBQuantityField;
    }
    // true = "With synchronization", false = "Without synchronization"
    public boolean isSynchronizedMode() {
        return syncOnRadio.isSelected();
    }
    public void setSimulationResult(String text) {
        simulationResult.setText(text);
    }
    public void show() {
        stage.showAndWait();
    }
}