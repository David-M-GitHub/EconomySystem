package de.imdacro.economySystem.database;

import java.util.HashMap;

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

    HashMap<String, Double> getTopBalances(int limit);
}
