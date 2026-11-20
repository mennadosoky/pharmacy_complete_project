package sample.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class HomeView {

    private final VBox root;

    private final Label totalValue = new Label("0");
    private final Label lowValue = new Label("0");
    private final Label outValue = new Label("0");
    private final Label stockValueLabel = new Label("0.00");
    private final Label salesCountValue = new Label("0");
    private final Label revenueValue = new Label("0.00");

    private final ListView<String> alertsList = new ListView<>();

    public HomeView() {

        root = new VBox(20);
        root.setPadding(new Insets(30));

        Label title = new Label("Dashboard");
        Label description = new Label("Overview of your pharmacy");

        VBox titleSection = new VBox(5, title, description);

        HBox row1 = new HBox(15,
                createCard("Total Medicines", totalValue),
                createCard("Low Stock", lowValue),
                createCard("Out of Stock", outValue)
        );

        HBox row2 = new HBox(15,
                createCard("Stock Value", stockValueLabel),
                createCard("Today's Sales", salesCountValue),
                createCard("Today's Revenue", revenueValue)
        );

        Label alertsTitle = new Label("Stock Alerts");

        alertsList.setPlaceholder(new Label("No stock alerts"));

        VBox.setVgrow(alertsList, Priority.ALWAYS);

        root.getChildren().addAll(
                titleSection,
                row1,
                row2,
                alertsTitle,
                alertsList
        );
    }

    private VBox createCard(String title, Label valueLabel) {

        Label titleLabel = new Label(title);

        VBox card = new VBox(8, titleLabel, valueLabel);

        card.setPadding(new Insets(15));
        card.setPrefWidth(200);

        return card;
    }

    public void update(int total,
                       int low,
                       int out,
                       double stockValue,
                       int todaySales,
                       double todayRevenue,
                       List<String> alerts) {

        totalValue.setText(String.valueOf(total));
        lowValue.setText(String.valueOf(low));
        outValue.setText(String.valueOf(out));
        stockValueLabel.setText(String.format("%.2f", stockValue));
        salesCountValue.setText(String.valueOf(todaySales));
        revenueValue.setText(String.format("%.2f", todayRevenue));

        alertsList.getItems().setAll(alerts);
    }

    public VBox getRoot() {
        return root;
    }
}
