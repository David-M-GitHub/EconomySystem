package de.imdacro.economySystem.commands;

import de.imdacro.economySystem.EconomySystem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Small helpers for tab completion in the plugin's commands.
 */
final class TabCompletion {

    private TabCompletion() {
    }

    /** @return the options that start with the given input, ignoring case */
    static @NotNull List<String> filter(@NotNull Collection<String> options, @NotNull String input) {
        return options.stream()
                .filter(option -> option.regionMatches(true, 0, input, 0, input.length()))
                .toList();
    }

    /** @return the names of online players matching the input, excluding the sender themselves */
    static @NotNull List<String> onlinePlayers(@NotNull CommandSender sender, @NotNull String input) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.getUniqueId().equals(sender instanceof Player self ? self.getUniqueId() : null))
                .map(Player::getName)
                .filter(name -> name.regionMatches(true, 0, input, 0, input.length()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * @return online players and players with an account whose name matches the input, without duplicates
     * @param excludeSelf leave the sender out of the suggestions
     */
    static @NotNull List<String> players(@NotNull EconomySystem plugin, @NotNull CommandSender sender,
                                         @NotNull String input, boolean excludeSelf) {
        // Only the (few) matches are collected into the sorted set
        Set<String> matches = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        matches.addAll(filter(plugin.getAccountNames().names(), input));
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().regionMatches(true, 0, input, 0, input.length())) {
                matches.add(online.getName());
            }
        }
        if (excludeSelf) {
            matches.remove(sender.getName());
        }
        return List.copyOf(matches);
    }
}
