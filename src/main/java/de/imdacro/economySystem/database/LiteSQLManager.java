package de.imdacro.economySystem.database;

import de.imdacro.economySystem.EconomySystem;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class LiteSQLManager implements DatabaseManager {

    private final EconomySystem plugin;
    private Connection connection;
    private final File databaseFile;

    public LiteSQLManager(EconomySystem plugin) {
        this.plugin = plugin;

        String fileName = plugin.getConfig().getString("database.litesql.file", "economy.db");
        this.databaseFile = new File(plugin.getDataFolder(), fileName);
    }

    @Override
    public void connect() {
        try {
            if (!databaseFile.exists()) {
                if (databaseFile.getParentFile().mkdirs()) {
                    plugin.getLogger().info("Created directories for LiteSQL database.");
                }
                if (databaseFile.createNewFile()) {
                    plugin.getLogger().info("LiteSQL database file created: " + databaseFile.getAbsolutePath());
                } else {
                    plugin.getLogger().warning("Failed to create LiteSQL database file.");
                }
            }

            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Connected to LiteSQL database at " + databaseFile.getName());

            createTables();

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create LiteSQL database file: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error connecting to LiteSQL database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createTables() {
        String createEconomyTable = "CREATE TABLE IF NOT EXISTS economy (" +
                "uuid TEXT PRIMARY KEY, " +
                "balance DOUBLE DEFAULT 0, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP);";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS economy_transactions (" +
                "uuid_from TEXT, " +
                "uuid_to TEXT, " +
                "amount DOUBLE, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP);";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createEconomyTable);
            stmt.executeUpdate(createTransactionsTable);
            plugin.getLogger().info("Tables created or already exist.");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error creating tables in LiteSQL!");
        }
    }

    public void createAccount(String uuid) {
        double startBalance = plugin.getConfig().getDouble("economy.start-balance");
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO economy (uuid, balance) VALUES (?, ?)")) {
            ps.setString(1, uuid);
            ps.setDouble(2, startBalance);
            ps.executeUpdate();
            plugin.getLogger().info("Account created for " + uuid + " with balance " + startBalance);
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error creating account for " + uuid);
        }
    }

    public boolean accountExists(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM economy WHERE uuid = ? LIMIT 1")) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double getBalance(String uuid) {
        double balance = 0;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT balance FROM economy WHERE uuid = ?");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                balance = resultSet.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error fetching balance for UUID: " + uuid);
        }
        return balance;
    }

    @Override
    public void setBalance(String uuid, double balance) {
        if (balance < 0) balance = 0;
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE economy SET balance = ? WHERE uuid = ?");
            statement.setDouble(1, balance);
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error setting balance for UUID: " + uuid);
        }
    }

    @Override
    public void addBalance(String uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    @Override
    public void removeBalance(String uuid, double amount) {
        double currentBalance = getBalance(uuid);
        if (currentBalance - amount < 0) {
            amount = currentBalance;
        }
        setBalance(uuid, currentBalance - amount);
    }

    public void createTransaction(String uuidFrom, String uuidTo, double amount) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO economy_transactions (uuid_from, uuid_to, amount) VALUES (?, ?, ?)")) {
            ps.setString(1, uuidFrom);
            ps.setString(2, uuidTo);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error creating transaction for " + uuidFrom + " to " + uuidTo);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("LiteSQL connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error closing LiteSQL connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
