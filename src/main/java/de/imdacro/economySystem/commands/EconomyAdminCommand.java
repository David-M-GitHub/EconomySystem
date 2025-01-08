package de.imdacro.economySystem.commands;

import de.imdacro.economySystem.EconomySystem;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class EconomyAdminCommand implements CommandExecutor {

    private final EconomySystem plugin;

    public EconomyAdminCommand(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length < 2) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin <set|add|remove|balance> <player> [amount]"));
            return true;
        }

        // Check if Player exists in OfflinePlayer list
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[1]);
        if (player == null) {
            commandSender.sendMessage(plugin.getMessages().get("player-not-found"));
            return true;
        }

        if (args[0].equalsIgnoreCase("balance")) {
            double balance = plugin.getDatabaseManager().getBalance(player.getUniqueId().toString());
            commandSender.sendMessage(plugin.getMessages().get("balance-message-other", "%player%", player.getName(), "%balance%", String.valueOf(balance)));
            return true;
        }

        if (args.length < 3) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin <set|add|remove> <player> <amount>"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin <set|add|remove> <player> <amount>"));
            return true;
        }

        if (amount <= 0) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin <set|add|remove> <player> <amount>"));
            return true;
        }

        switch (args[0]) {
            case "set":
                plugin.getDatabaseManager().setBalance(player.getUniqueId().toString(), amount);
                commandSender.sendMessage(plugin.getMessages().get("balance-set-success", "%player%", player.getName(), "%balance%", String.valueOf(amount)));
                break;
            case "add":
                plugin.getDatabaseManager().addBalance(player.getUniqueId().toString(), amount);
                commandSender.sendMessage(plugin.getMessages().get("balance-add-success", "%player%", player.getName(), "%balance%", String.valueOf(amount)));
                break;
            case "remove":
                plugin.getDatabaseManager().removeBalance(player.getUniqueId().toString(), amount);
                commandSender.sendMessage(plugin.getMessages().get("balance-remove-success", "%player%", player.getName(), "%balance%", String.valueOf(amount)));
                break;
            default:
                commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin <set|add|remove> <player> <amount>"));
                break;
        }


        return true;
    }
}
