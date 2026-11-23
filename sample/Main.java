package sample;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controller.MedicineController;
import sample.view.LoginView;
public class Main extends Application {
    // Change these two lines to change the login account
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "1234";
    private Stage stage;
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Pharmacy Management System");
        showLogin();
        stage.show();
    }
    // =========================================================
    // LOGIN SCREEN
    // =========================================================
    private void showLogin() {
        LoginView loginView =
                new LoginView();
        Runnable tryLogin = () -> {
            String username =
                    loginView.getUsernameField().getText().trim();
            String password =
                    loginView.getPasswordField().getText();
            if (USERNAME.equals(username)
                    && PASSWORD.equals(password)) {
                showApp();
            } else {
                loginView.showError(
                        "Wrong username or password."
                );
            }
        };
        loginView.getLoginButton()
                .setOnAction(event -> tryLogin.run());
        loginView.getPasswordField()
                .setOnAction(event -> tryLogin.run());
        stage.setScene(
                new Scene(
                        loginView.getRoot(),
                        400,
                        400
                )
        );
        stage.sizeToScene();
        stage.centerOnScreen();
    }
    // =========================================================
    // MAIN APPLICATION
    // =========================================================
    private void showApp() {
        // Logout goes back to the login screen
        MedicineController controller =
                new MedicineController(this::showLogin);
        stage.setScene(
                new Scene(
                        controller.getView().getRoot(),
                        1000,
                        700
                )
        );
        stage.sizeToScene();
        stage.centerOnScreen();
    }
    public static void main(String[] args) {
        launch(args);
    }
}