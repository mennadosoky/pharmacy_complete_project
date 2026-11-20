package sample.model;

public class Sale {

    private final String medicineName;
    private final int quantity;
    private final double total;
    private final String dateTime;

    public Sale(String medicineName, int quantity, double total, String dateTime) {
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.total = total;
        this.dateTime = dateTime;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return total;
    }

    public String getDateTime() {
        return dateTime;
    }
}
