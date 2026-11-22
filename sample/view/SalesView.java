package sample.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import sample.model.Medicine;
import sample.model.Sale;

public class SalesView {

    private final VBox root;

    private final ComboBox<Medicine> medicineBox = new ComboBox<>();
    private final TextField quantityField = new TextField();
    private final Label priceLabel = new Label("Unit price: -");
    private final Label totalLabel = new Label("Total: 0.00");
    private final Button sellButton = new Button("Sell");
    private final Button simulateButton = new Button("👥  Simulate 2 users");

    private final RadioButton syncOnRadio = new RadioButton("With synchronization");
    private final RadioButton syncOffRadio = new RadioButton("Without synchronization");

    // the two users of the simulation: each one chooses his own medicine and quantity
    private final ComboBox<Medicine> userAMedicineBox = new ComboBox<>();
    private final TextField userAQuantityField = new TextField();
    private final ComboBox<Medicine> userBMedicineBox = new ComboBox<>();
    private final TextField userBQuantityField = new TextField();

    private final Label todayLabel = new Label();

    private final TableView<Sale> salesTable = new TableView<>();

    public SalesView() {

        root = new VBox(20);
        root.setPadding(new Insets(30));

        Label title = new Label("Sales");
        Label description = new Label("Sell medicines and track your sales");

        VBox titleSection = new VBox(5, title, description);

        // ---------------- Sell form ----------------

        medicineBox.setPromptText("Select medicine");
        medicineBox.setPrefWidth(320);
        medicineBox.setPrefHeight(40);

        medicineBox.setConverter(new StringConverter<Medicine>() {

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

        quantityField.setPromptText("Quantity");
        quantityField.setPrefWidth(120);
        quantityField.setPrefHeight(40);

        sellButton.setPrefHeight(40);
        simulateButton.setPrefHeight(40);

        HBox formRow = new HBox(15, medicineBox, quantityField, sellButton);
        formRow.setAlignment(Pos.CENTER_LEFT);

        HBox infoRow = new HBox(30, priceLabel, totalLabel);

        // ---------------- Simulation mode (used by "Simulate 2 users") ----------------

        ToggleGroup modeGroup = new ToggleGroup();

        syncOnRadio.setToggleGroup(modeGroup);
        syncOffRadio.setToggleGroup(modeGroup);

        syncOnRadio.setSelected(true);

        HBox modeRow = new HBox(20,
                new Label("Simulation mode:"),
                syncOnRadio,
                syncOffRadio
        );

        modeRow.setAlignment(Pos.CENTER_LEFT);

        // ---------------- User A and User B (each chooses his own sale) ----------------

        setupUserBox(userAMedicineBox, userAQuantityField);
        setupUserBox(userBMedicineBox, userBQuantityField);

        Label userALabel = new Label("User A");
        Label userBLabel = new Label("User B");

        userALabel.setMinWidth(60);
        userBLabel.setMinWidth(60);

        HBox userARow = new HBox(15, userALabel, userAMedicineBox, userAQuantityField);
        HBox userBRow = new HBox(15, userBLabel, userBMedicineBox, userBQuantityField);

        userARow.setAlignment(Pos.CENTER_LEFT);
        userBRow.setAlignment(Pos.CENTER_LEFT);

        Label simulationTitle = new Label("Two users sell at the same time");

        VBox simulationBox = new VBox(10,
                simulationTitle,
                modeRow,
                userARow,
                userBRow,
                simulateButton
        );

        // ---------------- History table ----------------

        Label historyTitle = new Label("Sales History");

        TableColumn<Sale, String> nameColumn = new TableColumn<>("Medicine");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("medicineName"));

        TableColumn<Sale, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Sale, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        TableColumn<Sale, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        salesTable.getColumns().addAll(
                nameColumn,
                quantityColumn,
                totalColumn,
                dateColumn
        );

        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox.setVgrow(salesTable, Priority.ALWAYS);

        root.getChildren().addAll(
                titleSection,
                formRow,
                infoRow,
                todayLabel,
                simulationBox,
                historyTitle,
                salesTable
        );
    }

    // gives a user's combo box the same look/text as the main medicine box
    private void setupUserBox(ComboBox<Medicine> box, TextField quantity) {

        box.setPromptText("Select medicine");
        box.setPrefWidth(320);
        box.setPrefHeight(40);
        box.setConverter(medicineBox.getConverter());

        quantity.setPromptText("Quantity");
        quantity.setPrefWidth(120);
        quantity.setPrefHeight(40);
    }

    public void setTodaySummary(int count, double revenue) {

        todayLabel.setText(
                "Today: " + count
                        + " sales   |   Revenue: "
                        + String.format("%.2f", revenue)
        );
    }

    public VBox getRoot() {
        return root;
    }

    public ComboBox<Medicine> getMedicineBox() {
        return medicineBox;
    }

    public TextField getQuantityField() {
        return quantityField;
    }

    public Label getPriceLabel() {
        return priceLabel;
    }

    public Label getTotalLabel() {
        return totalLabel;
    }

    public Button getSellButton() {
        return sellButton;
    }

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

    // true = "With synchronization" is selected, false = "Without synchronization"
    public boolean isSynchronizedMode() {
        return syncOnRadio.isSelected();
    }

    public TableView<Sale> getSalesTable() {
        return salesTable;
    }
}
