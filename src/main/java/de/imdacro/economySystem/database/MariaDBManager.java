package de.imdacro.economySystem.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
    private HikariDataSource dataSource;
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

        String jdbcUrl = "jdbc:mariadb://" + host + ":" + port + "/" + database;

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.mariadb.jdbc.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(10000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);

            plugin.getLogger().info("Connected to MariaDB database at " + jdbcUrl);
            createTables();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MariaDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void createTables() {
        String economyTable = """
                CREATE TABLE IF NOT EXISTS economy (
                    uuid VARCHAR(36) PRIMARY KEY,
                    balance DOUBLE DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String transactionTable = """
                CREATE TABLE IF NOT EXISTS economy_transactions (
                    uuid_from VARCHAR(36),
                    uuid_to VARCHAR(36),
                    amount DOUBLE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(economyTable);
            stmt.executeUpdate(transactionTable);

            plugin.getLogger().info("Tables checked/created.");

        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -----------------------------------
    // Accounts
    // -----------------------------------

    public void createAccount(String uuid) {
        double startBalance = plugin.getConfig().getDouble("economy.start-balance");

        String sql = "INSERT INTO economy (uuid, balance) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ps.setDouble(2, startBalance);
            ps.executeUpdate();

            createTransaction("SERVER", uuid, startBalance);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating account for " + uuid);
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> createAccountAsync(String uuid) {
        return CompletableFuture.runAsync(() -> createAccount(uuid), dbExecutor);
    }

    public boolean accountExists(String uuid) {
        String sql = "SELECT 1 FROM economy WHERE uuid = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CompletableFuture<Boolean> accountExistsAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> accountExists(uuid), dbExecutor);
    }

    // -----------------------------------
    // BALANCE
    // -----------------------------------

    @Override
    public double getBalance(String uuid) {
        String sql = "SELECT balance FROM economy WHERE uuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error fetching balance for " + uuid);
            e.printStackTrace();
        }
        return 0.0;
    }

    public CompletableFuture<Double> getBalanceAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> getBalance(uuid), dbExecutor);
    }

    @Override
    public void setBalance(String uuid, double balance) {
        if (balance < 0) balance = 0;

        String sql = "UPDATE economy SET balance = ? WHERE uuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, balance);
            ps.setString(2, uuid);
            ps.executeUpdate();

            BalanceChangeEvent evt =
                    new BalanceChangeEvent(Bukkit.getPlayer(UUID.fromString(uuid)),
                            balance, !Bukkit.isPrimaryThread());

            Bukkit.getPluginManager().callEvent(evt);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting balance for " + uuid);
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> setBalanceAsync(String uuid, double balance) {
        return CompletableFuture.runAsync(() -> setBalance(uuid, balance), dbExecutor);
    }

    @Override
    public void addBalance(String uuid, double amount) {
        double current = getBalance(uuid);
        setBalance(uuid, current + amount);
    }

    @Override
    public void removeBalance(String uuid, double amount) {
        double current = getBalance(uuid);
        amount = Math.min(amount, current);
        setBalance(uuid, current - amount);
    }

    // -----------------------------------
    // TRANSACTIONS
    // -----------------------------------

    @Override
    public void createTransaction(String from, String to, double amount) {
        String sql = "INSERT INTO economy_transactions (uuid_from, uuid_to, amount) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, from);
            ps.setString(2, to);
            ps.setDouble(3, amount);
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating transaction.");
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> createTransactionAsync(String f, String t, double a) {
        return CompletableFuture.runAsync(() -> createTransaction(f, t, a), dbExecutor);
    }

    // -----------------------------------
    // TOP BALANCES
    // -----------------------------------

    @Override
    public LinkedHashMap<String, Double> getTopBalances(int limit) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT uuid, balance FROM economy ORDER BY balance DESC LIMIT " + limit;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("uuid"), rs.getDouble("balance"));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error fetching top balances!");
            e.printStackTrace();
        }

        return map;
    }

    public CompletableFuture<HashMap<String, Double>> getTopBalancesAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> getTopBalances(limit), dbExecutor);
    }

    // -----------------------------------
    // CLOSE
    // -----------------------------------

    @Override
    public void close() {
        dbExecutor.shutdown();

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("MariaDB connection pool closed.");
        }
    }
}
