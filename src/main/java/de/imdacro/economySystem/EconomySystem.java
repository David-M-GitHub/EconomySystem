package de.imdacro.economySystem;

import de.imdacro.economySystem.commands.BalanceCommand;
import de.imdacro.economySystem.database.DatabaseManager;
import de.imdacro.economySystem.database.LiteSQLManager;
import de.imdacro.economySystem.database.MariaDBManager;
import de.imdacro.economySystem.listener.PlayerJoinListener;
import de.imdacro.economySystem.utils.Messages;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class EconomySystem extends JavaPlugin {

    private Messages messages;
    private DatabaseManager databaseManager;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        loadMessages();
    }

    @Override
    public void onEnable() {

        // Register listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);

        // Register commands
        this.getCommand("balance").setExecutor(new BalanceCommand(this));

        setupDatabase();

        getServer().getConsoleSender().sendMessage(messages.get("plugin-enabled"));
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

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }

        getServer().getConsoleSender().sendMessage(messages.get("plugin-disabled"));
    }

    public Messages getMessages() {
        return messages;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
