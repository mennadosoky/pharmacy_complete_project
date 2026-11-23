package sample.controller;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
import java.util.concurrent.CountDownLatch;
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
        salesView.getUserAMedicineBox().setItems(allMedicines);
        salesView.getUserBMedicineBox().setItems(allMedicines);
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
                new AddMedicineView(allMedicines);
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
        // two users add stock to an existing medicine at the same time (demo)
        addView.getSimulateButton()
                .setOnAction(event -> {
                    simulateTwoStockAdds(addView);
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
            // synchronized in the service: duplicate check + save = ONE step
            service.addMedicine(
                    medicine
            );
            // the file is the source of truth: reload it into the screen list
            reloadMedicines();
            refreshTable();
            addView.getStage()
                    .close();
            showAlert(
                    "Success",
                    "Medicine added successfully."
            );
        } catch (IllegalStateException e) {
            // e.g. another thread already added this name
            showAlert(
                    "Cannot Add",
                    e.getMessage()
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
                        selectedMedicine,
                        allMedicines
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
        // two users edit two different medicines at the same time (demo)
        editView.getSimulateButton()
                .setOnAction(event -> {
                    simulateTwoEdits(editView);
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
            // synchronized in the service: read + check + write = ONE step
            service.updateMedicine(
                    oldMedicine,
                    updatedMedicine
            );
            reloadMedicines();
            refreshTable();
            editView.getStage()
                    .close();
            showAlert(
                    "Success",
                    "Medicine updated successfully."
            );
        } catch (IllegalStateException e) {
            // e.g. the medicine was changed/renamed by another thread
            reloadMedicines();
            showAlert(
                    "Cannot Update",
                    e.getMessage()
            );
        } catch (NumberFormatException e) {
            showAlert(
                    "Invalid Data",
                    "Price must be a number and quantity must be an integer."
            );
        }
    }
    // the file is the source of truth: reload it into the list shown on screen
    private void reloadMedicines() {
        allMedicines.setAll(
                service.loadMedicines()
        );
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
        salesView.getSimulateButton()
                .setOnAction(event -> simulateTwoUsers());
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
        Sale sale;
        try {
            // synchronized in the service:
            // check stock + deduct stock + record the sale = ONE step
            sale = service.sellMedicine(
                    selected.getName(),
                    quantity
            );
        } catch (IllegalStateException e) {
            // the stock changed since this screen was loaded
            salesView.getMedicineBox().setValue(null);
            reloadMedicines();
            showAlert(
                    "Sale Failed",
                    e.getMessage()
            );
            return;
        }
        // clear the form first, then update the lists
        salesView.getMedicineBox().setValue(null);
        salesView.getQuantityField().clear();
        reloadMedicines();
        allSales.add(0, sale);
        refreshTable();
        showAlert(
                "Sale Completed",
                quantity + " x " + sale.getMedicineName()
                        + "\nTotal: " + String.format("%.2f", sale.getTotal())
        );
    }
    // =========================================================
    // DEMO: TWO USERS SELL AT THE SAME TIME (two windows)
    // =========================================================
    // Starts two threads ("User A" and "User B") that sell the selected medicine
    // at the SAME moment. Each user gets his own result window.
    // Both threads call the synchronized service.sellMedicine(...), so
    // one sale succeeds and the other one is rejected if the stock is not enough.
    private void simulateTwoUsers() {
        // each user has his OWN medicine and his OWN quantity
        Medicine medicineA =
                salesView.getUserAMedicineBox().getValue();
        Medicine medicineB =
                salesView.getUserBMedicineBox().getValue();
        if (medicineA == null || medicineB == null) {
            showAlert(
                    "No Medicine Selected",
                    "Please select a medicine for User A and for User B."
            );
            return;
        }
        int quantityA =
                readQuantity(
                        salesView.getUserAQuantityField().getText(),
                        "User A"
                );
        if (quantityA < 0) {
            return;
        }
        int quantityB =
                readQuantity(
                        salesView.getUserBQuantityField().getText(),
                        "User B"
                );
        if (quantityB < 0) {
            return;
        }
        // radio button on the Sales page: with / without synchronization
        boolean useSync = salesView.isSynchronizedMode();
        // both threads wait here, then start at the same moment
        CountDownLatch go = new CountDownLatch(1);
        startUserThread("User A", 0, medicineA.getName(), quantityA, useSync, go);
        startUserThread("User B", 1, medicineB.getName(), quantityB, useSync, go);
        go.countDown();
    }
    // reads a whole number > 0; shows an alert and returns -1 if it is not valid
    private int readQuantity(String text, String user) {
        try {
            int quantity = Integer.parseInt(text.trim());
            if (quantity > 0) {
                return quantity;
            }
        } catch (NumberFormatException e) {
            // falls through to the alert
        }
        showAlert(
                "Invalid Data",
                user + ": quantity must be a whole number greater than zero."
        );
        return -1;
    }
    private void startUserThread(
            String user,
            int position,
            String medicineName,
            int quantity,
            boolean useSync,
            CountDownLatch go) {
        Thread thread = new Thread(() -> {
            Sale sale = null;
            String message;
            try {
                go.await();
                if (useSync) {
                    // synchronized: one sale at a time
                    sale = service.sellMedicine(medicineName, quantity);
                } else {
                    // NOT synchronized: shows the race condition
                    sale = service.sellMedicineWithoutSync(medicineName, quantity);
                }
                message = "Sale completed: " + quantity + " x " + medicineName
                        + "\nTotal: " + String.format("%.2f", sale.getTotal());
            } catch (IllegalStateException e) {
                // not enough stock (another user bought first)
                message = e.getMessage();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                message = "Sale was interrupted.";
            }
            final Sale doneSale = sale;
            final String doneMessage = message;
            // screens may only be changed from the JavaFX thread
            Platform.runLater(() -> {
                if (doneSale != null) {
                    allSales.add(0, doneSale);
                }
                reloadMedicines();
                showUserWindow(user, "Sales", position, doneSale != null, useSync, doneMessage, null);
            });
        });
        thread.setName(user);
        thread.setDaemon(true);
        thread.start();
    }
    // a small window that shows the result of ONE user's sale
    private void showUserWindow(
            String user,
            String page,
            int position,
            boolean success,
            boolean useSync,
            String message,
            javafx.stage.Window owner) {
        Label title = new Label(user + "  -  " + page);
        Label result =
                new Label(
                        "Mode: " + (useSync ? "WITH synchronization" : "WITHOUT synchronization")
                                + "\n\n"
                                + (success ? "SUCCESS\n\n" : "ERROR\n\n") + message
                );
        result.setWrapText(true);
        Button close = new Button("Close");
        VBox box = new VBox(15, title, result, close);
        box.setPadding(new Insets(25));
        Stage window = new Stage();
        // a window opened while the Add Medicine dialog is open must belong to it,
        // otherwise the modal dialog would block it
        if (owner != null) {
            window.initOwner(owner);
        }
        window.setTitle(user + " - " + page);
        window.setScene(new Scene(box, 340, 230));
        // put the two windows side by side
        window.setX(250 + position * 380);
        window.setY(250);
        close.setOnAction(event -> window.close());
        window.show();
    }
    // =========================================================
    // DEMO: TWO USERS ADD STOCK TO THE SAME MEDICINE AT THE SAME TIME
    // =========================================================
    // Both users add stock to an existing medicine at the SAME moment.
    //   WITHOUT synchronization: stock 5 + 5 + 5 ends as 10  (one addition is lost)
    //   WITH synchronization:    stock 5 + 5 + 5 ends as 15  (correct)
    private void simulateTwoStockAdds(AddMedicineView addView) {
        Medicine medicineA =
                addView.getUserAMedicineBox().getValue();
        Medicine medicineB =
                addView.getUserBMedicineBox().getValue();
        if (medicineA == null || medicineB == null) {
            showAlert(
                    "No Medicine Selected",
                    "Please select a medicine for User A and for User B."
            );
            return;
        }
        int amountA =
                readQuantity(
                        addView.getUserAQuantityField().getText(),
                        "User A"
                );
        if (amountA < 0) {
            return;
        }
        int amountB =
                readQuantity(
                        addView.getUserBQuantityField().getText(),
                        "User B"
                );
        if (amountB < 0) {
            return;
        }
        boolean useSync = addView.isSynchronizedMode();
        String nameA = medicineA.getName();
        boolean sameMedicine = nameA.equals(medicineB.getName());
        // the stock before, to show what the final stock SHOULD be
        int stockBefore = currentStock(nameA);
        addView.setSimulationResult("Running...");
        // both threads wait here, then start at the same moment
        CountDownLatch go = new CountDownLatch(1);
        Thread userA =
                startStockThread("User A", 0, nameA, amountA, useSync, go, addView.getStage());
        Thread userB =
                startStockThread("User B", 1, medicineB.getName(), amountB, useSync, go, addView.getStage());
        // waits for both users, then shows the final stock
        Thread summary = new Thread(() -> {
            try {
                userA.join();
                userB.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> {
                reloadMedicines();
                if (!sameMedicine) {
                    addView.setSimulationResult("Done. Check the Medicines table.");
                    return;
                }
                int expected = stockBefore + amountA + amountB;
                int actual = currentStock(nameA);
                addView.setSimulationResult(
                        "Final stock of " + nameA + ": " + actual
                                + "   (expected " + stockBefore + " + " + amountA
                                + " + " + amountB + " = " + expected + ")\n"
                                + (actual == expected
                                ? "CORRECT: no addition was lost."
                                : "WRONG: an addition was lost (race condition)!")
                );
            });
        });
        summary.setDaemon(true);
        summary.start();
        go.countDown();
    }
    private Thread startStockThread(
            String user,
            int position,
            String medicineName,
            int amount,
            boolean useSync,
            CountDownLatch go,
            javafx.stage.Window owner) {
        Thread thread = new Thread(() -> {
            Medicine result = null;
            String message;
            try {
                go.await();
                if (useSync) {
                    // synchronized: one addition at a time
                    result = service.addStock(medicineName, amount);
                } else {
                    // NOT synchronized: shows the lost update
                    result = service.addStockWithoutSync(medicineName, amount);
                }
                message = "Added " + amount + " x " + medicineName
                        + "\nStock after this user: " + result.getQuantity();
            } catch (IllegalStateException e) {
                message = e.getMessage();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                message = "Was interrupted.";
            }
            final boolean success = result != null;
            final String doneMessage = message;
            // screens may only be changed from the JavaFX thread
            Platform.runLater(() ->
                    showUserWindow(user, "Add Medicine", position, success, useSync, doneMessage, owner)
            );
        });
        thread.setName(user);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
    // the stock of a medicine, read from the file (-1 if it does not exist)
    private int currentStock(String name) {
        for (Medicine medicine : service.loadMedicines()) {
            if (medicine.getName().equals(name)) {
                return medicine.getQuantity();
            }
        }
        return -1;
    }
    // =========================================================
    // DEMO: TWO USERS EDIT TWO DIFFERENT MEDICINES AT THE SAME TIME
    // =========================================================
    // User A edits medicine X, User B edits medicine Y at the SAME moment.
    //   WITHOUT synchronization: each user writes back the WHOLE file he read
    //                            before the other edit -> one edit is lost
    //   WITH synchronization:    both edits are kept
    private void simulateTwoEdits(EditMedicineView editView) {
        Medicine medicineA =
                editView.getUserAMedicineBox().getValue();
        Medicine medicineB =
                editView.getUserBMedicineBox().getValue();
        if (medicineA == null || medicineB == null) {
            showAlert(
                    "No Medicine Selected",
                    "Please select a medicine for User A and for User B."
            );
            return;
        }
        if (medicineA.getName().equals(medicineB.getName())) {
            showAlert(
                    "Choose Two Different Medicines",
                    "User A and User B must edit two DIFFERENT medicines in this demo."
            );
            return;
        }
        int stockA =
                readStock(
                        editView.getUserAQuantityField().getText(),
                        "User A"
                );
        if (stockA < 0) {
            return;
        }
        int stockB =
                readStock(
                        editView.getUserBQuantityField().getText(),
                        "User B"
                );
        if (stockB < 0) {
            return;
        }
        boolean useSync = editView.isSynchronizedMode();
        String nameA = medicineA.getName();
        String nameB = medicineB.getName();
        editView.setSimulationResult("Running...");
        // both threads wait here, then start at the same moment
        CountDownLatch go = new CountDownLatch(1);
        Thread userA =
                startEditThread("User A", 0, medicineA, stockA, useSync, go, editView.getStage());
        Thread userB =
                startEditThread("User B", 1, medicineB, stockB, useSync, go, editView.getStage());
        // waits for both users, then checks that BOTH edits are in the file
        Thread summary = new Thread(() -> {
            try {
                userA.join();
                userB.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> {
                reloadMedicines();
                int actualA = currentStock(nameA);
                int actualB = currentStock(nameB);
                boolean correct = actualA == stockA && actualB == stockB;
                editView.setSimulationResult(
                        nameA + ": stock = " + actualA + "  (User A set " + stockA + ")\n"
                                + nameB + ": stock = " + actualB + "  (User B set " + stockB + ")\n"
                                + (correct
                                ? "CORRECT: both edits were kept."
                                : "WRONG: one edit was lost (race condition)!")
                );
            });
        });
        summary.setDaemon(true);
        summary.start();
        go.countDown();
    }
    private Thread startEditThread(
            String user,
            int position,
            Medicine medicine,
            int newStock,
            boolean useSync,
            CountDownLatch go,
            javafx.stage.Window owner) {
        Thread thread = new Thread(() -> {
            boolean success = false;
            String message;
            try {
                go.await();
                Medicine updated =
                        new Medicine(
                                medicine.getName(),
                                medicine.getPrice(),
                                newStock
                        );
                if (useSync) {
                    // synchronized: one edit at a time
                    service.updateMedicine(medicine, updated);
                } else {
                    // NOT synchronized: shows the lost edit
                    service.updateMedicineWithoutSync(medicine, updated);
                }
                success = true;
                message = "Edited " + medicine.getName()
                        + "\nStock set to " + newStock;
            } catch (IllegalStateException e) {
                message = e.getMessage();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                message = "Was interrupted.";
            }
            final boolean ok = success;
            final String doneMessage = message;
            // screens may only be changed from the JavaFX thread
            Platform.runLater(() ->
                    showUserWindow(user, "Edit Medicine", position, ok, useSync, doneMessage, owner)
            );
        });
        thread.setName(user);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
    // reads a whole number >= 0 (stock can be zero); alert + -1 if it is not valid
    private int readStock(String text, String user) {
        try {
            int stock = Integer.parseInt(text.trim());
            if (stock >= 0) {
                return stock;
            }
        } catch (NumberFormatException e) {
            // falls through to the alert
        }
        showAlert(
                "Invalid Data",
                user + ": new stock must be a whole number (0 or more)."
        );
        return -1;
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