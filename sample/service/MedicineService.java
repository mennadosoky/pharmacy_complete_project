package sample.service;

import sample.model.Medicine;
import sample.model.Sale;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe medicine service.
 *
 * Every method that reads or writes the data runs inside
 * synchronized (DataLock.LOCK). The operations that do "read -> check -> write"
 * (add, update, sell) hold the lock for the WHOLE operation, so no other
 * thread can change the data between the check and the write.
 *
 * Failures (duplicate name, not enough stock, ...) are reported with an
 * IllegalStateException carrying a message that can be shown to the user.
 */
public class MedicineService {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String filePath =
            "data/medicines.txt";

    private final SaleService saleService =
            new SaleService();

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

        synchronized (DataLock.LOCK) {

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

                        medicines.add(
                                new Medicine(
                                        parts[0].trim(),
                                        Double.parseDouble(parts[1].trim()),
                                        Integer.parseInt(parts[2].trim())
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
    }

    // =========================================================
    // ADD MEDICINE  (check duplicate + save = ONE atomic step)
    // =========================================================

    public void addMedicine(
            Medicine medicine) {

        synchronized (DataLock.LOCK) {

            // the duplicate check and the save can't be separated by another thread
            for (Medicine existing : loadMedicines()) {

                if (existing.getName()
                        .equalsIgnoreCase(medicine.getName())) {

                    throw new IllegalStateException(
                            "A medicine with this name already exists."
                    );
                }
            }

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
    }

    // =========================================================
    // ADD STOCK to an existing medicine  (read + add + write = ONE atomic step)
    // =========================================================

    public Medicine addStock(
            String name,
            int amount) {

        synchronized (DataLock.LOCK) {

            // always read the CURRENT stock, so no other user's addition is lost
            List<Medicine> medicines =
                    loadMedicines();

            for (int i = 0; i < medicines.size(); i++) {

                Medicine current = medicines.get(i);

                if (current.getName().equals(name)) {

                    Medicine updated =
                            new Medicine(
                                    current.getName(),
                                    current.getPrice(),
                                    current.getQuantity() + amount
                            );

                    medicines.set(i, updated);

                    saveMedicines(medicines);

                    return updated;
                }
            }

            throw new IllegalStateException(
                    "This medicine no longer exists."
            );
        }
    }

    // =========================================================
    // ADD STOCK WITHOUT SYNCHRONIZATION  (FOR THE DEMO ONLY)
    // =========================================================
    //
    // The WRONG way, kept only to show the "lost update" race condition:
    // two users read stock = 5, both add 5, both write 10 -> one addition is lost.
    // The real application must use addStock(...) above.

    public Medicine addStockWithoutSync(
            String name,
            int amount) {

        // 1. read
        List<Medicine> medicines =
                loadMedicines();

        for (int i = 0; i < medicines.size(); i++) {

            Medicine current = medicines.get(i);

            if (current.getName().equals(name)) {

                // the other user can read the same old stock during this pause
                try {

                    Thread.sleep(300);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }

                // 2. write (uses the OLD stock value that was read before the pause)
                Medicine updated =
                        new Medicine(
                                current.getName(),
                                current.getPrice(),
                                current.getQuantity() + amount
                        );

                medicines.set(i, updated);

                saveMedicines(medicines);

                return updated;
            }
        }

        throw new IllegalStateException(
                "This medicine no longer exists."
        );
    }

    // =========================================================
    // UPDATE MEDICINE  (read + check + write = ONE atomic step)
    // =========================================================

    public void updateMedicine(
            Medicine oldMedicine,
            Medicine updatedMedicine) {

        synchronized (DataLock.LOCK) {

            List<Medicine> medicines =
                    loadMedicines();

            int index = -1;

            for (int i = 0; i < medicines.size(); i++) {

                if (medicines.get(i).getName()
                        .equals(oldMedicine.getName())) {

                    index = i;

                    break;
                }
            }

            if (index == -1) {

                throw new IllegalStateException(
                        "This medicine no longer exists."
                );
            }

            for (int i = 0; i < medicines.size(); i++) {

                if (i != index
                        && medicines.get(i).getName()
                        .equalsIgnoreCase(updatedMedicine.getName())) {

                    throw new IllegalStateException(
                            "Another medicine already has this name."
                    );
                }
            }

            medicines.set(index, updatedMedicine);

            saveMedicines(medicines);
        }
    }

    // =========================================================
    // SELL MEDICINE  (check stock + deduct + record sale = ONE atomic step)
    // =========================================================

    public Sale sellMedicine(
            String name,
            int quantity) {

        synchronized (DataLock.LOCK) {

            // always read the CURRENT stock from the file, never a stale copy
            List<Medicine> medicines =
                    loadMedicines();

            int index = -1;

            for (int i = 0; i < medicines.size(); i++) {

                if (medicines.get(i).getName().equals(name)) {

                    index = i;

                    break;
                }
            }

            if (index == -1) {

                throw new IllegalStateException(
                        "This medicine no longer exists."
                );
            }

            Medicine current =
                    medicines.get(index);

            if (quantity > current.getQuantity()) {

                throw new IllegalStateException(
                        "Only " + current.getQuantity()
                                + " of " + current.getName()
                                + " left in stock."
                );
            }

            medicines.set(
                    index,
                    new Medicine(
                            current.getName(),
                            current.getPrice(),
                            current.getQuantity() - quantity
                    )
            );

            saveMedicines(medicines);

            Sale sale =
                    new Sale(
                            current.getName(),
                            quantity,
                            current.getPrice() * quantity,
                            LocalDateTime.now().format(FORMAT)
                    );

            // same lock (synchronized is re-entrant), so stock and sales stay consistent
            saleService.addSale(sale);

            return sale;
        }
    }

    // =========================================================
    // SELL MEDICINE WITHOUT SYNCHRONIZATION  (FOR THE DEMO ONLY)
    // =========================================================
    //
    // This is the WRONG way, kept only to show the race condition:
    // read -> check -> write are three separate steps with NO lock around them.
    // The short pause after the check lets another user read the same old stock,
    // so two users can both "succeed" and sell more than what exists.
    // The real application must use sellMedicine(...) above.

    public Sale sellMedicineWithoutSync(
            String name,
            int quantity) {

        // 1. read
        List<Medicine> medicines =
                loadMedicines();

        int index = -1;

        for (int i = 0; i < medicines.size(); i++) {

            if (medicines.get(i).getName().equals(name)) {

                index = i;

                break;
            }
        }

        if (index == -1) {

            throw new IllegalStateException(
                    "This medicine no longer exists."
            );
        }

        Medicine current =
                medicines.get(index);

        // 2. check
        if (quantity > current.getQuantity()) {

            throw new IllegalStateException(
                    "Only " + current.getQuantity()
                            + " of " + current.getName()
                            + " left in stock."
            );
        }

        // the other user can read the same old stock during this pause
        try {

            Thread.sleep(300);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // 3. write (uses the OLD stock value that was read before the pause)
        medicines.set(
                index,
                new Medicine(
                        current.getName(),
                        current.getPrice(),
                        current.getQuantity() - quantity
                )
        );

        saveMedicines(medicines);

        Sale sale =
                new Sale(
                        current.getName(),
                        quantity,
                        current.getPrice() * quantity,
                        LocalDateTime.now().format(FORMAT)
                );

        saleService.addSale(sale);

        return sale;
    }

    // =========================================================
    // SAVE ALL MEDICINES
    // =========================================================

    public void saveMedicines(
            List<Medicine> medicines) {

        synchronized (DataLock.LOCK) {

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
}
