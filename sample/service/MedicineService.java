package sample.service;

import sample.model.Medicine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedicineService {

    private final String filePath =
            "data/medicines.txt";

    // creates the data folder and the file if they don't exist
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
    // LOAD MEDICINES
    // =========================================================

    public List<Medicine> loadMedicines() {

        ensureFile();

        List<Medicine> medicines =
                new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath)
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                // skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts =
                        line.split(",");

                // skip broken lines
                if (parts.length < 3) {
                    continue;
                }

                try {

                    String name =
                            parts[0].trim();

                    double price =
                            Double.parseDouble(parts[1].trim());

                    int quantity =
                            Integer.parseInt(parts[2].trim());

                    medicines.add(
                            new Medicine(
                                    name,
                                    price,
                                    quantity
                            )
                    );

                } catch (NumberFormatException e) {
                    // skip broken line
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        return medicines;
    }

    // =========================================================
    // ADD MEDICINE
    // =========================================================

    public void addMedicine(
            Medicine medicine) {

        ensureFile();

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(
                                     filePath,
                                     true
                             )
                     )) {

            writer.write(
                    medicine.getName()
                            + ","
                            + medicine.getPrice()
                            + ","
                            + medicine.getQuantity()
            );

            writer.newLine();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // UPDATE MEDICINE
    // =========================================================

    public void updateMedicine(
            Medicine oldMedicine,
            Medicine updatedMedicine) {

        List<Medicine> medicines =
                loadMedicines();

        for (int i = 0;
             i < medicines.size();
             i++) {

            Medicine medicine =
                    medicines.get(i);

            if (medicine.getName()
                    .equals(oldMedicine.getName())) {

                medicines.set(
                        i,
                        updatedMedicine
                );

                break;
            }
        }

        saveMedicines(medicines);
    }

    // =========================================================
    // SAVE ALL MEDICINES
    // =========================================================

    public void saveMedicines(
            List<Medicine> medicines) {

        ensureFile();

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(filePath)
                     )) {

            for (Medicine medicine :
                    medicines) {

                writer.write(
                        medicine.getName()
                                + ","
                                + medicine.getPrice()
                                + ","
                                + medicine.getQuantity()
                );

                writer.newLine();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
