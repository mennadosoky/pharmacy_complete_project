package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import sample.model.Medicine;
import sample.model.Sale;
import sample.service.MedicineService;
import sample.service.SaleService;
import sample.service.SettingsService;
import sample.view.AddMedicineView;
import sample.view.EditMedicineView;
import sample.view.HomeView;
import sample.view.MedicineView;
import sample.view.ReportsView;
import sample.view.SalesView;
import sample.view.SettingsView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicineController {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MedicineView view;
    private final HomeView homeView;
    private final SalesView salesView;
    private final ReportsView reportsView;
    private final SettingsView settingsView;

    private final MedicineService service;
    private final SaleService saleService;
    private final SettingsService settings;

    private final ObservableList<Medicine> allMedicines;
    private final ObservableList<Sale> allSales;

    // called when the user logs out
    private final Runnable onLogout;

    public MedicineController(Runnable onLogout) {

        this.onLogout = onLogout;

        view = new MedicineView();
        homeView = new HomeView();
        salesView = new SalesView();
        reportsView = new ReportsView();
        settingsView = new SettingsView();

        service = new MedicineService();
        saleService = new SaleService();
        settings = new SettingsService();

        allMedicines =
                FXCollections.observableArrayList(
                        service.loadMedicines()
                );

        // newest sale first
        List<Sale> loadedSales = saleService.loadSales();
        Collections.reverse(loadedSales);

        allSales =
                FXCollections.observableArrayList(loadedSales);

        view.getMedicineTable().setItems(allMedicines);
        salesView.getMedicineBox().setItems(allMedicines);
        salesView.getSalesTable().setItems(allSales);

        applySettings();

        // every change in medicines or sales refreshes all pages
        allMedicines.addListener(
                (ListChangeListener<Medicine>) change -> refreshAll()
        );

        allSales.addListener(
                (ListChangeListener<Sale>) change -> refreshAll()
        );

        refreshAll();

        setupSearch();
        setupSearchButton();

        setupNavigation();

        setupAddButton();
        setupEditButton();

        setupSales();
        setupReports();
        setupSettings();
        setupLogoutButton();

        // first page shown after login
        view.showPage(homeView.getRoot());
    }

    // =========================================================
    // REFRESH EVERYTHING (stats, home, sales summary, report)
    // =========================================================

    private void refreshAll() {

        updateStatistics();
        updateHome();
        salesView.setTodaySummary(todaySalesCount(), todayRevenue());
        reportsView.setReportText(buildReport());
    }

    private void updateStatistics() {

        int total = allMedicines.size();
        int out = 0;

        for (Medicine medicine : allMedicines) {

            if (medicine.getQuantity() == 0) {
                out++;
            }
        }

        view.setStatistics(total, total - out, out);
    }

    private void updateHome() {

        int threshold = settings.getLowStockThreshold();

        int total = allMedicines.size();
        int low = 0;
        int out = 0;
        double stockValue = 0;

        List<String> alerts = new ArrayList<>();

        for (Medicine medicine : allMedicines) {

            stockValue += medicine.getPrice() * medicine.getQuantity();

            if (medicine.getQuantity() == 0) {

                out++;

                alerts.add(
                        "OUT OF STOCK: " + medicine.getName()
                );

            } else if (medicine.getQuantity() <= threshold) {

                low++;

                alerts.add(
                        "LOW STOCK: " + medicine.getName()
                                + " (" + medicine.getQuantity() + " left)"
                );
            }
        }

        homeView.update(
                total,
                low,
                out,
                stockValue,
                todaySalesCount(),
                todayRevenue(),
                alerts
        );
    }

    private int todaySalesCount() {

        String today = LocalDate.now().toString();

        int count = 0;

        for (Sale sale : allSales) {

            if (sale.getDateTime().startsWith(today)) {
                count++;
            }
        }

        return count;
    }

    private double todayRevenue() {

        String today = LocalDate.now().toString();

        double sum = 0;

        for (Sale sale : allSales) {

            if (sale.getDateTime().startsWith(today)) {
                sum += sale.getTotal();
            }
        }

        return sum;
    }

    // =========================================================
    // NAVIGATION  (Home / Medicines / Sales / Reports)
    // =========================================================

    private void setupNavigation() {

        view.getHomeButton()
                .setOnAction(event -> {

                    refreshAll();

                    view.showPage(homeView.getRoot());
                });

        view.getMedicinesButton()
                .setOnAction(event -> {

                    refreshTable();

                    view.showPage(view.getMedicinesPage());
                });

        view.getSalesButton()
                .setOnAction(event -> {

                    refreshAll();

                    view.showPage(salesView.getRoot());
                });

        view.getReportsButton()
                .setOnAction(event -> {

                    refreshAll();

                    view.showPage(reportsView.getRoot());
                });

        view.getSettingsButton()
                .setOnAction(event -> {

                    applySettings();

                    view.showPage(settingsView.getRoot());
                });
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        view.getSearchField()
                .textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            filterMedicines(
                                    newValue
                            );
                        }
                );
    }

    private void setupSearchButton() {

        view.getSearchButton()
                .setOnAction(event -> {

                    filterMedicines(
                            view.getSearchField().getText()
                    );
                });
    }

    private void filterMedicines(
            String searchText) {

        String search =
                searchText == null
                        ? ""
                        : searchText.toLowerCase().trim();

        if (search.isEmpty()) {

            view.getMedicineTable()
                    .setItems(allMedicines);

            return;
        }

        ObservableList<Medicine>
                filtered =
                FXCollections.observableArrayList();

        for (Medicine medicine :
                allMedicines) {

            if (medicine.getName()
                    .toLowerCase()
                    .startsWith(search)) {

                filtered.add(medicine);
            }
        }

        view.getMedicineTable()
                .setItems(filtered);
    }

    // re-apply the current search text to the table
    private void refreshTable() {

        filterMedicines(
                view.getSearchField().getText()
        );

        view.getMedicineTable().refresh();
    }

    // =========================================================
    // ADD MEDICINE
    // =========================================================

    private void setupAddButton() {

        view.getAddButton()
                .setOnAction(event -> {

                    addMedicine();
                });
    }

    private void addMedicine() {

        AddMedicineView addView =
                new AddMedicineView();

        addView.getSaveButton()
                .setOnAction(event -> {

                    saveNewMedicine(
                            addView
                    );
                });

        addView.getCancelButton()
                .setOnAction(event -> {

                    addView.getStage()
                            .close();
                });

        addView.show();
    }

    private void saveNewMedicine(
            AddMedicineView addView) {

        try {

            String name =
                    addView.getNameField()
                            .getText()
                            .trim();

            String priceText =
                    addView.getPriceField()
                            .getText()
                            .trim();

            String quantityText =
                    addView.getQuantityField()
                            .getText()
                            .trim();

            if (name.isEmpty()) {

                showAlert(
                        "Invalid Data",
                        "Medicine name cannot be empty."
                );

                return;
            }

            if (name.contains(",")) {

                showAlert(
                        "Invalid Data",
                        "Medicine name cannot contain a comma."
                );

                return;
            }

            if (nameExists(name, null)) {

                showAlert(
                        "Invalid Data",
                        "A medicine with this name already exists."
                );

                return;
            }

            double price =
                    Double.parseDouble(
                            priceText
                    );

            if (price < 0) {

                showAlert(
                        "Invalid Data",
                        "Price cannot be negative."
                );

                return;
            }

            int quantity =
                    Integer.parseInt(
                            quantityText
                    );

            if (quantity < 0) {

                showAlert(
                        "Invalid Data",
                        "Quantity cannot be negative."
                );

                return;
            }

            Medicine medicine =
                    new Medicine(
                            name,
                            price,
                            quantity
                    );

            // save to file
            service.addMedicine(
                    medicine
            );

            // add to the list (listener refreshes the pages)
            allMedicines.add(
                    medicine
            );

            refreshTable();

            addView.getStage()
                    .close();

            showAlert(
                    "Success",
                    "Medicine added successfully."
            );

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Data",
                    "Price must be a number and quantity must be an integer."
            );
        }
    }

    // =========================================================
    // EDIT MEDICINE
    // =========================================================

    private void setupEditButton() {

        view.getEditButton()
                .setOnAction(event -> {

                    editSelectedMedicine();
                });
    }

    private void editSelectedMedicine() {

        // the table lives on the medicines page
        view.showPage(view.getMedicinesPage());

        Medicine selectedMedicine =
                view.getMedicineTable()
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedMedicine == null) {

            showAlert(
                    "No Medicine Selected",
                    "Please select a medicine from the table first."
            );

            return;
        }

        EditMedicineView editView =
                new EditMedicineView(
                        selectedMedicine
                );

        editView.getSaveButton()
                .setOnAction(event -> {

                    saveEditedMedicine(
                            selectedMedicine,
                            editView
                    );
                });

        editView.getCancelButton()
                .setOnAction(event -> {

                    editView.getStage()
                            .close();
                });

        editView.show();
    }

    private void saveEditedMedicine(
            Medicine oldMedicine,
            EditMedicineView editView) {

        try {

            String name =
                    editView.getNameField()
                            .getText()
                            .trim();

            double price =
                    Double.parseDouble(
                            editView
                                    .getPriceField()
                                    .getText()
                                    .trim()
                    );

            int quantity =
                    Integer.parseInt(
                            editView
                                    .getQuantityField()
                                    .getText()
                                    .trim()
                    );

            if (name.isEmpty()) {

                showAlert(
                        "Invalid Data",
                        "Medicine name cannot be empty."
                );

                return;
            }

            if (name.contains(",")) {

                showAlert(
                        "Invalid Data",
                        "Medicine name cannot contain a comma."
                );

                return;
            }

            if (nameExists(name, oldMedicine)) {

                showAlert(
                        "Invalid Data",
                        "Another medicine already has this name."
                );

                return;
            }

            if (price < 0) {

                showAlert(
                        "Invalid Data",
                        "Price cannot be negative."
                );

                return;
            }

            if (quantity < 0) {

                showAlert(
                        "Invalid Data",
                        "Quantity cannot be negative."
                );

                return;
            }

            Medicine updatedMedicine =
                    new Medicine(
                            name,
                            price,
                            quantity
                    );

            service.updateMedicine(
                    oldMedicine,
                    updatedMedicine
            );

            int index =
                    allMedicines.indexOf(
                            oldMedicine
                    );

            allMedicines.set(
                    index,
                    updatedMedicine
            );

            refreshTable();

            editView.getStage()
                    .close();

            showAlert(
                    "Success",
                    "Medicine updated successfully."
            );

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Data",
                    "Price must be a number and quantity must be an integer."
            );
        }
    }

    // true if another medicine already uses this name
    private boolean nameExists(String name, Medicine exclude) {

        for (Medicine medicine : allMedicines) {

            if (medicine != exclude
                    && medicine.getName().equalsIgnoreCase(name)) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // SALES
    // =========================================================

    private void setupSales() {

        // update unit price / total while the user types
        salesView.getMedicineBox()
                .valueProperty()
                .addListener((observable, oldValue, newValue) -> updateSaleTotal());

        salesView.getQuantityField()
                .textProperty()
                .addListener((observable, oldValue, newValue) -> updateSaleTotal());

        salesView.getSellButton()
                .setOnAction(event -> sellMedicine());
    }

    private void updateSaleTotal() {

        Medicine selected =
                salesView.getMedicineBox().getValue();

        if (selected == null) {

            salesView.getPriceLabel().setText("Unit price: -");
            salesView.getTotalLabel().setText("Total: 0.00");

            return;
        }

        salesView.getPriceLabel().setText(
                "Unit price: " + String.format("%.2f", selected.getPrice())
        );

        double total = 0;

        try {

            int quantity =
                    Integer.parseInt(
                            salesView.getQuantityField().getText().trim()
                    );

            if (quantity > 0) {
                total = selected.getPrice() * quantity;
            }

        } catch (NumberFormatException e) {
            // total stays 0
        }

        salesView.getTotalLabel().setText(
                "Total: " + String.format("%.2f", total)
        );
    }

    private void sellMedicine() {

        Medicine selected =
                salesView.getMedicineBox().getValue();

        if (selected == null) {

            showAlert(
                    "No Medicine Selected",
                    "Please select a medicine to sell."
            );

            return;
        }

        int quantity;

        try {

            quantity =
                    Integer.parseInt(
                            salesView.getQuantityField().getText().trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Data",
                    "Quantity must be a whole number."
            );

            return;
        }

        if (quantity <= 0) {

            showAlert(
                    "Invalid Data",
                    "Quantity must be greater than zero."
            );

            return;
        }

        if (quantity > selected.getQuantity()) {

            showAlert(
                    "Not Enough Stock",
                    "Only " + selected.getQuantity()
                            + " of " + selected.getName()
                            + " left in stock."
            );

            return;
        }

        double total =
                selected.getPrice() * quantity;

        Medicine updated =
                new Medicine(
                        selected.getName(),
                        selected.getPrice(),
                        selected.getQuantity() - quantity
                );

        Sale sale =
                new Sale(
                        selected.getName(),
                        quantity,
                        total,
                        LocalDateTime.now().format(FORMAT)
                );

        // save to files
        service.updateMedicine(selected, updated);
        saleService.addSale(sale);

        // clear the form first, then update the lists
        salesView.getMedicineBox().setValue(null);
        salesView.getQuantityField().clear();

        int index =
                allMedicines.indexOf(selected);

        allMedicines.set(index, updated);

        allSales.add(0, sale);

        refreshTable();

        showAlert(
                "Sale Completed",
                quantity + " x " + sale.getMedicineName()
                        + "\nTotal: " + String.format("%.2f", total)
        );
    }

    // =========================================================
    // REPORTS
    // =========================================================

    private void setupReports() {

        reportsView.getRefreshButton()
                .setOnAction(event -> {

                    reportsView.setReportText(buildReport());
                });

        reportsView.getExportButton()
                .setOnAction(event -> exportReport());
    }

    private String buildReport() {

        int threshold = settings.getLowStockThreshold();

        int out = 0;
        int low = 0;
        double stockValue = 0;

        StringBuilder outList = new StringBuilder();
        StringBuilder lowList = new StringBuilder();

        for (Medicine medicine : allMedicines) {

            stockValue += medicine.getPrice() * medicine.getQuantity();

            if (medicine.getQuantity() == 0) {

                out++;

                outList.append("  - ")
                        .append(medicine.getName())
                        .append("\n");

            } else if (medicine.getQuantity() <= threshold) {

                low++;

                lowList.append("  - ")
                        .append(medicine.getName())
                        .append(" (")
                        .append(medicine.getQuantity())
                        .append(" left)\n");
            }
        }

        int itemsSold = 0;
        double totalRevenue = 0;

        for (Sale sale : allSales) {

            itemsSold += sale.getQuantity();
            totalRevenue += sale.getTotal();
        }

        StringBuilder report = new StringBuilder();

        report.append("===== ")
                .append(settings.getPharmacyName())
                .append(" - REPORT =====\n");

        report.append("Generated: ")
                .append(LocalDateTime.now().format(FORMAT))
                .append("\n\n");

        report.append("--- STOCK ---\n");
        report.append("Total medicines : ").append(allMedicines.size()).append("\n");
        report.append("Low stock       : ").append(low).append("\n");
        report.append("Out of stock    : ").append(out).append("\n");
        report.append("Stock value     : ").append(String.format("%.2f", stockValue)).append("\n\n");

        report.append("--- SALES ---\n");
        report.append("Today's sales   : ").append(todaySalesCount()).append("\n");
        report.append("Today's revenue : ").append(String.format("%.2f", todayRevenue())).append("\n");
        report.append("All sales       : ").append(allSales.size()).append("\n");
        report.append("Items sold      : ").append(itemsSold).append("\n");
        report.append("Total revenue   : ").append(String.format("%.2f", totalRevenue)).append("\n\n");

        report.append("--- OUT OF STOCK ---\n");
        report.append(outList.length() == 0 ? "  none\n" : outList);

        report.append("\n--- LOW STOCK ---\n");
        report.append(lowList.length() == 0 ? "  none\n" : lowList);

        return report.toString();
    }

    private void exportReport() {

        try {

            File file = new File("data/report.txt");

            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            try (BufferedWriter writer =
                         new BufferedWriter(new FileWriter(file))) {

                writer.write(buildReport());
            }

            showAlert(
                    "Report Saved",
                    "The report was saved to data/report.txt"
            );

        } catch (IOException e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Could not save the report."
            );
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void setupSettings() {

        settingsView.getSaveButton()
                .setOnAction(event -> saveSettings());
    }

    // push saved settings into the screens
    private void applySettings() {

        view.setPharmacyName(settings.getPharmacyName());
        view.setUserName(settings.getUserName());
        view.setLowStockThreshold(settings.getLowStockThreshold());

        settingsView.setValues(
                settings.getPharmacyName(),
                settings.getUserName(),
                settings.getLowStockThreshold()
        );
    }

    private void saveSettings() {

        String pharmacyName =
                settingsView.getPharmacyNameField().getText().trim();

        String userName =
                settingsView.getUserNameField().getText().trim();

        if (pharmacyName.isEmpty() || userName.isEmpty()) {

            showAlert(
                    "Invalid Data",
                    "Pharmacy name and user name cannot be empty."
            );

            return;
        }

        int threshold;

        try {

            threshold =
                    Integer.parseInt(
                            settingsView.getThresholdField().getText().trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Data",
                    "Low stock limit must be a whole number."
            );

            return;
        }

        if (threshold < 0) {

            showAlert(
                    "Invalid Data",
                    "Low stock limit cannot be negative."
            );

            return;
        }

        settings.setPharmacyName(pharmacyName);
        settings.setUserName(userName);
        settings.setLowStockThreshold(threshold);
        settings.save();

        applySettings();
        refreshAll();

        showAlert(
                "Success",
                "Settings saved."
        );
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private void setupLogoutButton() {

        view.getLogoutButton()
                .setOnAction(event -> {

                    Alert confirm =
                            new Alert(
                                    Alert.AlertType.CONFIRMATION,
                                    "Are you sure you want to log out?"
                            );

                    confirm.setTitle("Logout");

                    confirm.setHeaderText(null);

                    confirm.showAndWait()
                            .ifPresent(result -> {

                                if (result == ButtonType.OK) {

                                    onLogout.run();
                                }
                            });
                });
    }

    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    public MedicineView getView() {

        return view;
    }
}
