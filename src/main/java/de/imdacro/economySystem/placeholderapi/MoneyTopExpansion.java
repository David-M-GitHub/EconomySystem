package de.imdacro.economySystem.placeholderapi;

import de.imdacro.economySystem.EconomySystem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MoneyTopExpansion extends PlaceholderExpansion {

    private final EconomySystem plugin;

    public MoneyTopExpansion(EconomySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "economysystem";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ImDacro";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.startsWith("leaderboard_")) {
            // Format: leaderboard_1_name / leaderboard_1_value
            String[] parts = params.split("_");
            if (parts.length != 3) return null;

            int pos;
            try {
                pos = Integer.parseInt(parts[1]) - 1; // 1 → index 0
            } catch (NumberFormatException e) {
                return null;
            }

            String type = parts[2]; // "name" or "value"

            // Top 10
            LinkedHashMap<String, Double> top = plugin.getDatabaseManager().getTopBalances(10);
            List<Map.Entry<String, Double>> list = new ArrayList<>(top.entrySet());

            if (pos < 0 || pos >= list.size())
                return ""; // Out of bounds

            Map.Entry<String, Double> entry = list.get(pos);
            String uuid = entry.getKey();
            double balance = entry.getValue();

            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

            if (type.equalsIgnoreCase("name")) {
                // TODO Messages with Unknown into config
                return p.getName() != null ? p.getName() : "Unknown";
            }

            if (type.equalsIgnoreCase("value")) {
                return String.valueOf(balance);
            }
        }

        return null;
    }
}
