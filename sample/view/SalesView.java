package sample.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

        HBox formRow = new HBox(15, medicineBox, quantityField, sellButton);
        formRow.setAlignment(Pos.CENTER_LEFT);

        HBox infoRow = new HBox(30, priceLabel, totalLabel);

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
                historyTitle,
                salesTable
        );
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

    public TableView<Sale> getSalesTable() {
        return salesTable;
    }
}
