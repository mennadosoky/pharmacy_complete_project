package sample.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginView {

    private final VBox root;

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("Login");
    private final Label errorLabel = new Label();

    public LoginView() {

        Label title = new Label("FCI PHARMA CARE");
        Label subtitle = new Label("Please log in to continue");

        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        loginButton.setPrefWidth(250);
        loginButton.setPrefHeight(40);

        errorLabel.setStyle("-fx-text-fill: red;");

        Label hint = new Label("Default login: admin / 1234");

        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        root.getChildren().addAll(
                title,
                subtitle,
                usernameField,
                passwordField,
                loginButton,
                errorLabel,
                hint
        );
    }

    public VBox getRoot() {
        return root;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }
}
