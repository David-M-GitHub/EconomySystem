package de.imdacro.economySystem.commands;

import de.imdacro.economySystem.EconomySystem;
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
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (strings.length < 3) {
            commandSender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin <set|add|remove> <player> <amount>"));
            return true;
        }

        // TODO: Implement the economy admin command



        return true;
    }
}
