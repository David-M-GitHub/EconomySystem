package de.imdacro.economySystem.commands;

import de.imdacro.economySystem.EconomySystem;
import de.imdacro.economySystem.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class BalanceTopCommand implements CommandExecutor {

    private final EconomySystem plugin;

    public BalanceTopCommand(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        // Get Top 10 players with the highest balance
        HashMap<String, Double> topBalances = plugin.getDatabaseManager().getTopBalances(10);

        // Send the top balances to the command sender
        commandSender.sendMessage(plugin.getMessages().get("top-list-title"));
        for (int i = 0; i < topBalances.size(); i++) {
            String uuid = (String) topBalances.keySet().toArray()[i];
            double balance = topBalances.get(uuid);

            commandSender.sendMessage(plugin.getMessages().get("top-list-entry", "%position%", String.valueOf(i + 1), "%player%", plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName(), "%balance%", String.valueOf(balance)));
        }
        commandSender.sendMessage(plugin.getMessages().get("top-list-footer"));



        return true;
    }
}
