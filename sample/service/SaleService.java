package sample.service;

import sample.model.Sale;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe sales log. Uses the same DataLock as MedicineService.
 */
public class SaleService {

    private final String filePath = "data/sales.txt";

    private void ensureFile() {

        try {

            File file = new File(filePath);

            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // LOAD SALES  (file format: name,quantity,total,dateTime)
    // =========================================================

    public List<Sale> loadSales() {

        synchronized (DataLock.LOCK) {

            ensureFile();

            List<Sale> sales = new ArrayList<>();

            try (BufferedReader reader =
                         new BufferedReader(new FileReader(filePath))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split(",");

                    if (parts.length < 4) {
                        continue;
                    }

                    try {

                        sales.add(
                                new Sale(
                                        parts[0].trim(),
                                        Integer.parseInt(parts[1].trim()),
                                        Double.parseDouble(parts[2].trim()),
                                        parts[3].trim()
                                )
                        );

                    } catch (NumberFormatException e) {
                        // skip broken line
                    }
                }

            } catch (IOException e) {

                e.printStackTrace();
            }

            return sales;
        }
    }

    // =========================================================
    // ADD SALE
    // =========================================================

    public void addSale(Sale sale) {

        synchronized (DataLock.LOCK) {

            ensureFile();

            try (BufferedWriter writer =
                         new BufferedWriter(new FileWriter(filePath, true))) {

                writer.write(
                        sale.getMedicineName()
                                + ","
                                + sale.getQuantity()
                                + ","
                                + sale.getTotal()
                                + ","
                                + sale.getDateTime()
                );

                writer.newLine();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}
