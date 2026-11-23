package sample.service;
/**
 * ONE lock shared by every service.
 *
 * All the data (medicines.txt and sales.txt) is protected by this single lock,
 * so only ONE thread at a time can read-check-write the data.
 * It is static, so it works even if several service objects are created.
 */
final class DataLock {
    static final Object LOCK = new Object();
    private DataLock() {
    }
}