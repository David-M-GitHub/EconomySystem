package de.imdacro.economySystem.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BalanceChangeEvent extends Event {

        private final Player player;

        private final double newAmount;

        public BalanceChangeEvent(Player player, double newAmount, boolean async) {
            super(async);
            this.player = player;
            this.newAmount = newAmount;
        }

        private static final HandlerList handlers = new HandlerList();

        public HandlerList getHandlers() {
            return handlers;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }

        public Player getTarget() {
            return this.player;
        }

        public double getNewAmount() {
            return this.newAmount;
        }
}
