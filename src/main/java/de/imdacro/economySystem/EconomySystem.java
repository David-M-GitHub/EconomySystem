package de.imdacro.economySystem;

import de.imdacro.economySystem.commands.BalanceCommand;
import de.imdacro.economySystem.commands.BalanceTopCommand;
import de.imdacro.economySystem.commands.EconomyAdminCommand;
import de.imdacro.economySystem.commands.PayCommand;
import de.imdacro.economySystem.database.DatabaseManager;
import de.imdacro.economySystem.database.LiteSQLManager;
import de.imdacro.economySystem.database.MariaDBManager;
import de.imdacro.economySystem.listener.PlayerJoinListener;
import de.imdacro.economySystem.placeholderapi.MoneyTopExpansion;
import de.imdacro.economySystem.utils.AccountNames;
import de.imdacro.economySystem.utils.Messages;
import de.imdacro.economySystem.utils.Metrics;
import de.imdacro.economySystem.vault.ReflectionVault;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class EconomySystem extends JavaPlugin {

    private Messages messages;
    private DatabaseManager databaseManager;
    private final AccountNames accountNames = new AccountNames(this);

    @Override
    public void onLoad() {
        saveDefaultConfig();
        loadMessages();
    }

    @Override
    public void onEnable() {

        // Metrics Data
        int pluginId = 27089;
        Metrics metrics = new Metrics(this, pluginId);


        metrics.addCustomChart(new Metrics.SimplePie("currency_type", () -> {
            return getConfig().getString("economy.currency-name");
        }));

        // PlaceholderAPI support
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MoneyTopExpansion(this).register();
            getLogger().info("MoneyTop PlaceholderAPI Expansion loaded!");
        }

        // Register listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);

        // Register commands
        registerCommand("balance", new BalanceCommand(this));
        registerCommand("pay", new PayCommand(this));
        registerCommand("economyadmin", new EconomyAdminCommand(this));
        registerCommand("balancetop", new BalanceTopCommand(this));

        setupDatabase();
        if (databaseManager != null) {
            accountNames.load();
        }

        // Vault integration (optional)
        new ReflectionVault(this);

        getServer().getConsoleSender().sendMessage(messages.get("plugin-enabled") + "");
    }

    private void registerCommand(String name, org.bukkit.command.TabExecutor handler) {
        org.bukkit.command.PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is missing from plugin.yml!");
            return;
        }
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }

    private void loadMessages() {
        try {
            File file = new File(getDataFolder(), "messages.json");
            if (!file.exists()) {
                saveResource("messages.json", false);
            }
            messages = new Messages(file);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Error loading the Messages file!");
        }
    }


    private void setupDatabase() {
        String dbType = getConfig().getString("database.type", "litesql");
        switch (dbType.toLowerCase()) {
            case "mariadb":
                databaseManager = new MariaDBManager(this);
                break;
            case "litesql":
                databaseManager = new LiteSQLManager(this);
                break;
            default:
                getLogger().severe("Invalid Database type: " + dbType);
                getServer().getPluginManager().disablePlugin(this);
                return;
        }
        databaseManager.connect();
    }

    public String formatBalance(double balance) {
        return String.format("%,.0f", balance);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }

        getServer().getConsoleSender().sendMessage(messages.get("plugin-disabled") + "");
    }

    public Messages getMessages() {
        return messages;
    }

    public AccountNames getAccountNames() {
        return accountNames;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

}
