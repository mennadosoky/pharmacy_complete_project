package sample.view;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
public class ReportsView {
    private final VBox root;
    private final TextArea reportArea = new TextArea();
    private final Button refreshButton = new Button("Refresh");
    private final Button exportButton = new Button("Export to File");
    public ReportsView() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        Label title = new Label("Reports");
        Label description = new Label("Stock and sales summary");
        VBox titleSection = new VBox(5, title, description);
        reportArea.setEditable(false);
        reportArea.setStyle("-fx-font-family: 'Consolas', 'Monospaced';");
        VBox.setVgrow(reportArea, Priority.ALWAYS);
        HBox buttons = new HBox(10, refreshButton, exportButton);
        root.getChildren().addAll(
                titleSection,
                buttons,
                reportArea
        );
    }
    public void setReportText(String text) {
        reportArea.setText(text);
    }
    public VBox getRoot() {
        return root;
    }
    public Button getRefreshButton() {
        return refreshButton;
    }
    public Button getExportButton() {
        return exportButton;
    }
}