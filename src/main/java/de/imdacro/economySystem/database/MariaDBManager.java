package de.imdacro.economySystem.database;

import de.imdacro.economySystem.EconomySystem;

import java.sql.*;

public class MariaDBManager implements DatabaseManager {

    private final EconomySystem plugin;
    private Connection connection;

    public MariaDBManager(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        String host = plugin.getConfig().getString("database.mariadb.host", "localhost");
        int port = plugin.getConfig().getInt("database.mariadb.port", 3306);
        String database = plugin.getConfig().getString("database.mariadb.database", "economy");
        String username = plugin.getConfig().getString("database.mariadb.username", "root");
        String password = plugin.getConfig().getString("database.mariadb.password", "");

        String url = "jdbc:mariadb://" + host + ":" + port + "/" + database;

        try {
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Connected to MariaDB database at " + host + ":" + port + "/" + database);

            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error connecting to MariaDB database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createTables() {
        String createEconomyTable = "CREATE TABLE IF NOT EXISTS economy (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "balance DOUBLE DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS economy_transactions (" +
                "uuid_from VARCHAR(36), " +
                "uuid_to VARCHAR(36), " +
                "amount DOUBLE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createEconomyTable);
            stmt.executeUpdate(createTransactionsTable);
            plugin.getLogger().info("Tables created or already exist.");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error creating tables in MariaDB!");
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

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("MariaDB connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error closing MariaDB connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
