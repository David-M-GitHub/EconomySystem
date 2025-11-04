package de.imdacro.economySystem.database;

import de.imdacro.economySystem.EconomySystem;
import de.imdacro.economySystem.events.BalanceChangeEvent;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MariaDBManager implements DatabaseManager {

    private final EconomySystem plugin;
    private Connection connection;
    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(4);

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
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Connected to MariaDB database at " + host + ":" + port + "/" + database);

            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error connecting to MariaDB database: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("MariaDB JDBC driver not found: " + e.getMessage());
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
            createTransaction("SERVER", uuid, startBalance);
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error creating account for " + uuid);
        }
    }

    public CompletableFuture<Void> createAccountAsync(String uuid) {
        return CompletableFuture.runAsync(() -> createAccount(uuid), dbExecutor);
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

    public CompletableFuture<Boolean> accountExistsAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> accountExists(uuid), dbExecutor);
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

    public CompletableFuture<Double> getBalanceAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> getBalance(uuid), dbExecutor);
    }

    @Override
    public void setBalance(String uuid, double balance) {
        if (balance < 0) balance = 0;
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE economy SET balance = ? WHERE uuid = ?");
            statement.setDouble(1, balance);
            statement.setString(2, uuid);
            statement.executeUpdate();

            // Call Event
            BalanceChangeEvent balanceChangeEvent = new BalanceChangeEvent(Bukkit.getPlayer(UUID.fromString(uuid)), balance, !Bukkit.isPrimaryThread());
            Bukkit.getPluginManager().callEvent(balanceChangeEvent);

        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error setting balance for UUID: " + uuid);
        }
    }

    public CompletableFuture<Void> setBalanceAsync(String uuid, double balance) {
        return CompletableFuture.runAsync(() -> getBalance(uuid), dbExecutor);
    }

    @Override
    public void addBalance(String uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public CompletableFuture<Void> addBalanceAsync(String uuid, double amount) {
        return getBalanceAsync(uuid).thenCompose(current -> setBalanceAsync(uuid, current + amount));
    }

    @Override
    public void removeBalance(String uuid, double amount) {
        double currentBalance = getBalance(uuid);
        if (currentBalance - amount < 0) {
            amount = currentBalance;
        }
        setBalance(uuid, currentBalance - amount);
    }

    public CompletableFuture<Void> removeBalanceAsync(String uuid, double amount) {
        return getBalanceAsync(uuid).thenCompose(current -> {
            double toRemove = Math.min(amount, current);
            return setBalanceAsync(uuid, current - toRemove);
        });
    }

    @Override
    public void createTransaction(String uuidFrom, String uuidTo, double amount) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO economy_transactions (uuid_from, uuid_to, amount) VALUES (?, ?, ?)")) {
            ps.setString(1, uuidFrom);
            ps.setString(2, uuidTo);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error creating transaction from " + uuidFrom + " to " + uuidTo + " with amount " + amount);
        }
    }

    public CompletableFuture<Void> createTransactionAsync(String uuidFrom, String uuidTo, double amount) {
        return CompletableFuture.runAsync(() -> createTransaction(uuidFrom, uuidTo, amount), dbExecutor);
    }

    @Override
    public HashMap<String, Double> getTopBalances(int limit) {
        LinkedHashMap<String, Double> topBalances = new LinkedHashMap<>();
        try (Statement stmt = connection.createStatement()) {
            String query = "SELECT uuid, balance FROM economy ORDER BY balance DESC LIMIT " + limit;
            plugin.getLogger().info("Executing query: " + query);
            ResultSet resultSet = stmt.executeQuery(query);

            int rowCount = 0;
            while (resultSet.next()) {
                rowCount++;
                String uuid = resultSet.getString("uuid");
                double balance = resultSet.getDouble("balance");
                plugin.getLogger().info("Processing row " + rowCount + ": UUID=" + uuid + ", Balance=" + balance);

                topBalances.put(uuid, balance);
                plugin.getLogger().info("Added player to top list: " + uuid + " with balance " + balance);
            }
            plugin.getLogger().info("Total rows processed: " + rowCount + ", Final map size: " + topBalances.size());
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error fetching top balances!");
        }
        return topBalances;
    }

    public CompletableFuture<HashMap<String, Double>> getTopBalancesAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> getTopBalances(limit), dbExecutor);
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
        dbExecutor.shutdown();
        plugin.getLogger().info("DB executor shutdown initiated.");
    }

    public Connection getConnection() {
        return connection;
    }
}
