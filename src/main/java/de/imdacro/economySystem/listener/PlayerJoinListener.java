package de.imdacro.economySystem.listener;

import de.imdacro.economySystem.EconomySystem;
import de.imdacro.economySystem.database.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final EconomySystem plugin;

    public PlayerJoinListener(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        DatabaseManager dbManager = plugin.getDatabaseManager();

        // Create account if it doesn't exist
        if (!dbManager.accountExists(uuid)) {
            dbManager.createAccount(uuid);
        }
    }
}
