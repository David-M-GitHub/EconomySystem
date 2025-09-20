package de.imdacro.economySystem.commands;

import de.imdacro.economySystem.EconomySystem;
import de.imdacro.economySystem.database.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

    private final EconomySystem plugin;

    public BalanceCommand(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessages().get("player-not-player"));
            return true;
        }

        DatabaseManager databaseManager = plugin.getDatabaseManager();

        if (args.length == 0) {
            String uuid = player.getUniqueId().toString();
            if (!databaseManager.accountExists(uuid)) {
                databaseManager.createAccount(uuid);
            }
            double balance = databaseManager.getBalance(uuid);

            player.sendMessage(plugin.getMessages().get("balance-message-own", "%balance%", String.valueOf(balance)));
            return true;
        }

        if (!player.hasPermission("economysystem.command.balance.other")) {
            player.sendMessage(plugin.getMessages().get("no-permission"));
            return true;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(plugin.getMessages().get("player-not-found"));
            return true;
        }

        String targetUuid = target.getUniqueId().toString();
        if (!databaseManager.accountExists(targetUuid)) {
            databaseManager.createAccount(targetUuid);
        }
        double balance = databaseManager.getBalance(targetUuid);
        player.sendMessage(plugin.getMessages().get("balance-message-other", "%player%", target.getName(), "%balance%", String.valueOf(balance)));

        return true;
    }
}
