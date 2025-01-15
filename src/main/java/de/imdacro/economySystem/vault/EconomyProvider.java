package de.imdacro.economySystem.vault;

import java.util.List;

import de.imdacro.economySystem.EconomySystem;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;

public class EconomyProvider extends AbstractEconomy {

  private final EconomySystem plugin;

  public EconomyProvider(EconomySystem plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public String getName() {
    return plugin.getConfig().getString("economy.currency-name");
  }
  
  public boolean hasBankSupport() {
    return false;
  }
  
  public int fractionalDigits() {
    return 0;
  }

  public String format(double amount) {
    return plugin.formatBalance(amount);
  }
  
  public String currencyNamePlural() {
    return plugin.getConfig().getString("economy.currency-name");
  }
  
  public String currencyNameSingular() {
    return plugin.getConfig().getString("economy.currency-name");
  }
  
  public boolean hasAccount(String playerName) {
    return plugin.getDatabaseManager().accountExists(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString());
  }
  
  public boolean hasAccount(String playerName, String worldName) {
    return plugin.getDatabaseManager().accountExists(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString());
  }
  
  public double getBalance(String playerName) {
    return plugin.getDatabaseManager().getBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString());
  }
  
  public double getBalance(String playerName, String world) {
    return plugin.getDatabaseManager().getBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString());
  }
  
  public boolean has(String playerName, double amount) {
    return plugin.getDatabaseManager().getBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString()) >= amount;
  }
  
  public boolean has(String playerName, String worldName, double amount) {
    return plugin.getDatabaseManager().getBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString()) >= amount;
  }
  
  public EconomyResponse withdrawPlayer(String playerName, double amount) {
    plugin.getDatabaseManager().removeBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), amount);
    plugin.getDatabaseManager().createTransaction(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), "VAULT", amount);
    return new EconomyResponse(amount, 0.0D, EconomyResponse.ResponseType.SUCCESS, "Error withdrawing money");
  }
  
  public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
    plugin.getDatabaseManager().removeBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), amount);
    plugin.getDatabaseManager().createTransaction(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), "VAULT", amount);
    return new EconomyResponse(amount, 0.0D, EconomyResponse.ResponseType.SUCCESS, "Error withdrawing money");
  }
  
  public EconomyResponse depositPlayer(String playerName, double amount) {
    plugin.getDatabaseManager().addBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), amount);
    plugin.getDatabaseManager().createTransaction("VAULT", Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), amount);
    return new EconomyResponse(amount, 0.0D, EconomyResponse.ResponseType.SUCCESS, "Error depositing money");
  }
  
  public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
    plugin.getDatabaseManager().addBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), amount);
    plugin.getDatabaseManager().createTransaction("VAULT", Bukkit.getOfflinePlayer(playerName).getUniqueId().toString(), amount);
    return new EconomyResponse(amount, 0.0D, EconomyResponse.ResponseType.SUCCESS, "Error depositing money");
  }
  
  public EconomyResponse createBank(String name, String player) {
    return null;
  }
  
  public EconomyResponse deleteBank(String name) {
    return null;
  }
  
  public EconomyResponse bankBalance(String name) {
    return null;
  }
  
  public EconomyResponse bankHas(String name, double amount) {
    return null;
  }
  
  public EconomyResponse bankWithdraw(String name, double amount) {
    return null;
  }
  
  public EconomyResponse bankDeposit(String name, double amount) {
    return null;
  }
  
  public EconomyResponse isBankOwner(String name, String playerName) {
    return null;
  }
  
  public EconomyResponse isBankMember(String name, String playerName) {
    return null;
  }
  
  public List<String> getBanks() {
    return null;
  }
  
  public boolean createPlayerAccount(String playerName) {
    plugin.getDatabaseManager().createAccount(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString());
    return true;
  }
  
  public boolean createPlayerAccount(String playerName, String worldName) {
    plugin.getDatabaseManager().createAccount(Bukkit.getOfflinePlayer(playerName).getUniqueId().toString());
    return true;
  }
}