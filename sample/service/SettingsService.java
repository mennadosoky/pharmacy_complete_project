package sample.service;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
public class SettingsService {
    private final String filePath = "data/settings.properties";
    private final Properties props = new Properties();
    public SettingsService() {
        load();
    }
    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void save() {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                props.store(writer, "Pharmacy settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getPharmacyName() {
        return props.getProperty("pharmacyName", "FCI PHARMA CARE");
    }
    public void setPharmacyName(String value) {
        props.setProperty("pharmacyName", value);
    }
    public String getUserName() {
        return props.getProperty("userName", "Menna");
    }
    public void setUserName(String value) {
        props.setProperty("userName", value);
    }
    public int getLowStockThreshold() {
        try {
            return Integer.parseInt(
                    props.getProperty("lowStockThreshold", "10")
            );
        } catch (NumberFormatException e) {
            return 10;
        }
    }
    public void setLowStockThreshold(int value) {
        props.setProperty("lowStockThreshold", String.valueOf(value));
    }
}