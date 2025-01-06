package de.imdacro.economySystem.vault;

import de.imdacro.economySystem.EconomySystem;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

public class Vault {
  private static Permission permission;
  
  private final EconomySystem plugin;
  
  private EconomyProvider economy;
  
  public Vault(EconomySystem plugin) {
    this.plugin = plugin;
    hook();
  }

  
  private void hook() {
    try {
      if (this.economy == null)
        this.economy = new EconomyProvider(plugin);
      ServicesManager sm = this.plugin.getServer().getServicesManager();
      sm.register(Economy.class, this.economy, this.plugin, ServicePriority.High);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}