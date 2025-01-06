package de.imdacro.economySystem.commands;

import de.imdacro.economySystem.EconomySystem;
import de.imdacro.economySystem.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PayCommand implements CommandExecutor {

    private final EconomySystem plugin;

    public PayCommand(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (strings.length < 2) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
            return true;
        }

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(plugin.getMessages().get("player-not-player"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(strings[1]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
            return true;
        }

        if (amount <= 0) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
            return true;
        }

        Player target = Bukkit.getPlayer(strings[0]);
        if (target == null) {
            commandSender.sendMessage(plugin.getMessages().get("player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            commandSender.sendMessage(plugin.getMessages().get("pay-self"));
            return true;
        }

        DatabaseManager databaseManager = plugin.getDatabaseManager();
        double playerBalance = databaseManager.getBalance(player.getUniqueId().toString());

        if (playerBalance < amount) {
            commandSender.sendMessage(plugin.getMessages().get("not-enough-money"));
            return true;
        }

        databaseManager.removeBalance(player.getUniqueId().toString(), amount);
        databaseManager.addBalance(target.getUniqueId().toString(), amount);

        player.sendMessage(plugin.getMessages().get("pay-success-sender", "%player%", target.getName(), "%amount%", String.valueOf(amount)));
        target.sendMessage(plugin.getMessages().get("pay-success-receiver", "%player%", player.getName(), "%amount%", String.valueOf(amount)));

        return true;
    }
}
