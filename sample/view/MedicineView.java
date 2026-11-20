package sample.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import sample.model.Medicine;

public class MedicineView {

    // =========================
    // Main Root
    // =========================

    private final BorderPane root;

    // =========================
    // Header
    // =========================

    private Label pharmacyLabel;
    private Label userLabel;

    // =========================
    // Medicines page controls
    // =========================

    private TextField searchField;
    private Button searchButton;

    private TableView<Medicine> medicineTable;

    private Label totalValueLabel;
    private Label availableValueLabel;
    private Label outOfStockValueLabel;

    private VBox medicinesPage;

    private int lowStockThreshold = 10;

    // =========================
    // Sidebar buttons
    // =========================

    private Button homeButton;
    private Button medicinesButton;
    private Button salesButton;
    private Button reportsButton;

    private Button addButton;
    private Button editButton;
    private Button settingsButton;

    private Button logoutButton;

    public MedicineView() {

        root = new BorderPane();

        createLayout();
    }

    // =========================
    // Main Layout
    // =========================

    private void createLayout() {

        root.setTop(createHeader());

        root.setLeft(createSidebar());

        medicinesPage = createMainContent();

        root.setCenter(medicinesPage);
    }

    // =========================================================
    // HEADER
    // =========================================================

    private HBox createHeader() {

        pharmacyLabel =
                new Label("FCI PHARMA CARE");

        Label subtitle =
                new Label("Pharmacy Management System");

        VBox branding =
                new VBox(3);

        branding.getChildren().addAll(
                pharmacyLabel,
                subtitle
        );

        userLabel =
                new Label("Mohammed  ● Online");

        HBox header =
                new HBox();

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(
                        15,
                        25,
                        15,
                        25
                )
        );

        header.setSpacing(20);

        header.getChildren().addAll(
                branding,
                userLabel
        );

        HBox.setHgrow(
                branding,
                Priority.ALWAYS
        );

        return header;
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private VBox createSidebar() {

        VBox sidebar =
                new VBox(10);

        sidebar.setPadding(
                new Insets(20)
        );

        sidebar.setPrefWidth(220);

        // -------------------------
        // Dashboard Section
        // -------------------------

        Label dashboardTitle =
                new Label("DASHBOARD");

        homeButton =
                createMenuButton("🏠  Home");

        medicinesButton =
                createMenuButton("💊  Medicines");

        salesButton =
                createMenuButton("🛒  Sales");

        reportsButton =
                createMenuButton("📊  Reports");

        // -------------------------
        // Management Section
        // -------------------------

        Label managementTitle =
                new Label("MANAGEMENT");

        addButton =
                createMenuButton("➕  Add Medicine");

        editButton =
                createMenuButton("✏  Edit Medicine");

        settingsButton =
                createMenuButton("⚙  Settings");

        // -------------------------
        // Spacer
        // -------------------------

        VBox spacer =
                new VBox();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        // -------------------------
        // Logout
        // -------------------------

        logoutButton =
                createMenuButton("🚪  Logout");

        sidebar.getChildren().addAll(

                dashboardTitle,

                homeButton,
                medicinesButton,
                salesButton,
                reportsButton,

                managementTitle,

                addButton,
                editButton,
                settingsButton,

                spacer,

                logoutButton
        );

        return sidebar;
    }

    // =========================================================
    // MENU BUTTON
    // =========================================================

    private Button createMenuButton(String text) {

        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPrefHeight(42);

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        return button;
    }

    // =========================================================
    // MEDICINES PAGE (main content)
    // =========================================================

    private VBox createMainContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(30)
        );

        Label title =
                new Label("Medicine Search");

        Label description =
                new Label(
                        "Search and manage available medicines"
                );

        VBox titleSection =
                new VBox(5);

        titleSection.getChildren().addAll(
                title,
                description
        );

        HBox searchSection =
                createSearchSection();

        HBox statistics =
                createStatistics();

        Label tableTitle =
                new Label("Available Medicines");

        TableView<Medicine> table =
                createMedicineTable();

        content.getChildren().addAll(

                titleSection,

                searchSection,

                statistics,

                tableTitle,

                table
        );

        VBox.setVgrow(
                table,
                Priority.ALWAYS
        );

        return content;
    }

    // =========================================================
    // SEARCH AREA
    // =========================================================

    private HBox createSearchSection() {

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search medicine name..."
        );

        searchField.setPrefHeight(45);

        searchButton =
                new Button("Search");

        searchButton.setPrefHeight(45);

        HBox searchBox =
                new HBox(10);

        searchBox.setAlignment(
                Pos.CENTER_LEFT
        );

        searchBox.getChildren().addAll(
                searchField,
                searchButton
        );

        HBox.setHgrow(
                searchField,
                Priority.ALWAYS
        );

        return searchBox;
    }

    // =========================================================
    // STATISTICS CARDS
    // =========================================================

    private HBox createStatistics() {

        HBox cards =
                new HBox(15);

        totalValueLabel =
                new Label("0");

        availableValueLabel =
                new Label("0");

        outOfStockValueLabel =
                new Label("0");

        cards.getChildren().addAll(

                createCard("Total Medicines", totalValueLabel),

                createCard("Available", availableValueLabel),

                createCard("Out of Stock", outOfStockValueLabel)
        );

        return cards;
    }

    private VBox createCard(
            String title,
            Label valueLabel
    ) {

        Label titleLabel =
                new Label(title);

        VBox card =
                new VBox(8);

        card.setPadding(
                new Insets(15)
        );

        card.setPrefWidth(200);

        card.getChildren().addAll(

                titleLabel,

                valueLabel
        );

        return card;
    }

    // =========================================================
    // MEDICINE TABLE
    // =========================================================

    private TableView<Medicine> createMedicineTable() {

        medicineTable =
                new TableView<>();

        TableColumn<Medicine, String> nameColumn =
                new TableColumn<>("Medicine");

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        TableColumn<Medicine, Double> priceColumn =
                new TableColumn<>("Price");

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price")
        );

        TableColumn<Medicine, Integer> quantityColumn =
                new TableColumn<>("Stock");

        quantityColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        // Status is calculated from the quantity
        TableColumn<Medicine, String> statusColumn =
                new TableColumn<>("Status");

        statusColumn.setCellValueFactory(
                data -> {

                    int quantity =
                            data.getValue().getQuantity();

                    String status;

                    if (quantity == 0) {

                        status = "● Out of Stock";

                    } else if (quantity <= lowStockThreshold) {

                        status = "● Low Stock";

                    } else {

                        status = "● Available";
                    }

                    return new ReadOnlyStringWrapper(status);
                }
        );

        medicineTable.getColumns().addAll(

                nameColumn,

                priceColumn,

                quantityColumn,

                statusColumn
        );

        nameColumn.setPrefWidth(250);

        priceColumn.setPrefWidth(150);

        quantityColumn.setPrefWidth(150);

        statusColumn.setPrefWidth(180);

        medicineTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        return medicineTable;
    }

    // =========================================================
    // PUBLIC HELPERS
    // =========================================================

    // Swap the page shown in the center of the window
    public void showPage(Node page) {

        root.setCenter(page);
    }

    public void setPharmacyName(String name) {

        pharmacyLabel.setText(name);
    }

    public void setUserName(String name) {

        userLabel.setText(name + "  ● Online");
    }

    public void setStatistics(int total, int available, int outOfStock) {

        totalValueLabel.setText(String.valueOf(total));

        availableValueLabel.setText(String.valueOf(available));

        outOfStockValueLabel.setText(String.valueOf(outOfStock));
    }

    public void setLowStockThreshold(int threshold) {

        this.lowStockThreshold = threshold;

        medicineTable.refresh();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public BorderPane getRoot() {
        return root;
    }

    public VBox getMedicinesPage() {
        return medicinesPage;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public TableView<Medicine> getMedicineTable() {
        return medicineTable;
    }

    public Button getHomeButton() {
        return homeButton;
    }

    public Button getMedicinesButton() {
        return medicinesButton;
    }

    public Button getSalesButton() {
        return salesButton;
    }

    public Button getReportsButton() {
        return reportsButton;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getEditButton() {
        return editButton;
    }

    public Button getSettingsButton() {
        return settingsButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }
}
