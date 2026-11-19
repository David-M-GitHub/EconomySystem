package de.imdacro.economySystem.commands;

import de.imdacro.economySystem.EconomySystem;
import de.imdacro.economySystem.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BalanceTopCommand implements TabExecutor {

    private final EconomySystem plugin;

    public BalanceTopCommand(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        // Get Top 10 players with the highest balance
        HashMap<String, Double> topBalances = plugin.getDatabaseManager().getTopBalances(10);

        plugin.getLogger().info("BalanceTop: Found " + topBalances.size() + " players");

        // Send the top balances to the command sender
        commandSender.sendMessage(plugin.getMessages().get("top-list-title"));
        if (topBalances.isEmpty()) {
            commandSender.sendMessage(plugin.getMessages().get("no-players-found"));
        } else {
            for (int i = 0; i < topBalances.size(); i++) {
                String uuid = (String) topBalances.keySet().toArray()[i];
                double balance = topBalances.get(uuid);

                String playerName = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
                if (playerName == null) {
                    // Fallback to UUID short representation when the player has never joined or name is unknown
                    playerName = uuid;
                }

                commandSender.sendMessage(plugin.getMessages().get("top-list-entry", "%position%", String.valueOf(i + 1), "%player%", playerName, "%balance%", String.valueOf(balance)));
            }
        }
        // Don't send empty footer



        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}
