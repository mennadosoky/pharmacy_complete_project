package sample.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class SettingsView {

    private final VBox root;

    private final TextField pharmacyNameField = new TextField();
    private final TextField userNameField = new TextField();
    private final TextField thresholdField = new TextField();

    private final Button saveButton = new Button("Save Settings");

    public SettingsView() {

        root = new VBox(20);
        root.setPadding(new Insets(30));

        Label title = new Label("Settings");
        Label description = new Label("Customize your pharmacy system");

        VBox titleSection = new VBox(5, title, description);

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);

        form.add(new Label("Pharmacy Name"), 0, 0);
        form.add(pharmacyNameField, 1, 0);

        form.add(new Label("User Name"), 0, 1);
        form.add(userNameField, 1, 1);

        form.add(new Label("Low Stock Limit"), 0, 2);
        form.add(thresholdField, 1, 2);

        Label note = new Label(
                "A medicine is 'Low Stock' when its quantity is "
                        + "between 1 and the Low Stock Limit."
        );

        pharmacyNameField.setPrefWidth(300);

        root.getChildren().addAll(
                titleSection,
                form,
                note,
                saveButton
        );
    }

    public void setValues(String pharmacyName, String userName, int threshold) {

        pharmacyNameField.setText(pharmacyName);
        userNameField.setText(userName);
        thresholdField.setText(String.valueOf(threshold));
    }

    public VBox getRoot() {
        return root;
    }

    public TextField getPharmacyNameField() {
        return pharmacyNameField;
    }

    public TextField getUserNameField() {
        return userNameField;
    }

    public TextField getThresholdField() {
        return thresholdField;
    }

    public Button getSaveButton() {
        return saveButton;
    }
}
