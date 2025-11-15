package de.imdacro.economySystem.database;

import java.util.LinkedHashMap;

public interface DatabaseManager {
    void connect();
    void close();

    boolean accountExists(String uuid);
    void createAccount(String uuid);

    double getBalance(String uuid);
    void setBalance(String uuid, double balance);
    void addBalance(String uuid, double amount);
    void removeBalance(String uuid, double amount);
    void createTransaction(String uuidFrom, String uuidTo, double amount);

    LinkedHashMap<String, Double> getTopBalances(int limit);
}
