package de.imdacro.economySystem.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Public API event, fired after a player's balance has been changed and saved.
 * <p>
 * This event may be fired asynchronously (see {@link #isAsynchronous()}), so listeners
 * must not touch the Bukkit world directly when it is async.
 * It is not cancellable, as the change has already been persisted.
 * <p>
 * Example:
 * <pre>{@code
 * @EventHandler
 * public void onBalanceChange(BalanceChangeEvent event) {
 *     getLogger().info(event.getUniqueId() + ": " + event.getOldAmount() + " -> " + event.getNewAmount());
 * }
 * }</pre>
 */
public class BalanceChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final Player player;
    private final double oldAmount;
    private final double newAmount;

    /**
     * @param uuid      the account owner
     * @param player    the account owner if online, otherwise {@code null}
     * @param oldAmount the balance before the change
     * @param newAmount the balance after the change
     * @param async     whether the event is fired off the primary thread
     */
    public BalanceChangeEvent(@NotNull UUID uuid, @Nullable Player player, double oldAmount, double newAmount, boolean async) {
        super(async);
        this.uuid = uuid;
        this.player = player;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }

    /** @return the UUID of the account whose balance changed (also set when the owner is offline) */
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    /** @return the account owner, which may be offline */
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /** @return the online player, or {@code null} if the account owner is offline */
    public @Nullable Player getTarget() {
        return player;
    }

    /** @return the balance before the change */
    public double getOldAmount() {
        return oldAmount;
    }

    /** @return the balance after the change */
    public double getNewAmount() {
        return newAmount;
    }

    /** @return the difference between the new and old balance */
    public double getDifference() {
        return newAmount - oldAmount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
