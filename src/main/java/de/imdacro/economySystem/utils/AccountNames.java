package de.imdacro.economySystem.utils;

import de.imdacro.economySystem.EconomySystem;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory index of the names of all players that have an account, used for tab completion.
 * <p>
 * It is filled once on startup and kept up to date as accounts are created or players join,
 * so tab completion never has to query the database.
 */
public class AccountNames {

    private final EconomySystem plugin;
    private final Map<UUID, String> names = new ConcurrentHashMap<>();

    public AccountNames(@NotNull EconomySystem plugin) {
        this.plugin = plugin;
    }

    /** Loads all existing accounts: the query runs async, the name lookups on the main thread. */
    public void load() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> uuids = plugin.getDatabaseManager().getAllAccountUuids();
            Bukkit.getScheduler().runTask(plugin, () -> uuids.forEach(this::add));
        });
    }

    /** Adds an account, or updates the stored name if the player has renamed. */
    public void add(@NotNull String uuid) {
        try {
            UUID id = UUID.fromString(uuid);
            String name = Bukkit.getOfflinePlayer(id).getName();
            if (name != null) {
                names.put(id, name);
            }
        } catch (IllegalArgumentException ignored) {
            // not a valid UUID, skip
        }
    }

    /** @return a live view of the known names, in no particular order */
    public @NotNull Collection<String> names() {
        return names.values();
    }
}
