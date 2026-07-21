package de.imdacro.economySystem.vault;

import de.imdacro.economySystem.EconomySystem;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import java.lang.reflect.*;
import java.util.UUID;

public class ReflectionVault {
  private final EconomySystem plugin;

  public ReflectionVault(EconomySystem plugin) {
    this.plugin = plugin;
    hook();
  }

  private void hook() {
    try {
      // Check whether Vault API is present
      Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
      Class<?> econRespClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
      Class<?> respTypeClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse$ResponseType");

      ServicesManager sm = this.plugin.getServer().getServicesManager();

      Object proxy = Proxy.newProxyInstance(econClass.getClassLoader(), new Class<?>[]{econClass}, (proxyObj, method, args) -> {
        String name = method.getName();
        try {
          switch (name) {
            case "getName":
              return plugin.getConfig().getString("economy.currency-name");
            case "isEnabled":
              return true;
            case "hasAccount": {
              UUID id = getTargetUUID(args[0]);
              if (id == null) return false;
              return plugin.getDatabaseManager().accountExists(id.toString());
            }
            case "createPlayerAccount": {
              UUID id = getTargetUUID(args[0]);
              if (id == null) return false;
              plugin.getDatabaseManager().createAccount(id.toString());
              return true;
            }
            case "getBalance": {
              UUID id = getTargetUUID(args[0]);
              if (id == null) return 0.0D;
              return plugin.getDatabaseManager().getBalance(id.toString());
            }
            case "has": {
              UUID id = getTargetUUID(args[0]);
              if (id == null) return false;
              double amount = ((Number) args[1]).doubleValue();
              return plugin.getDatabaseManager().getBalance(id.toString()) >= amount;
            }
            case "withdrawPlayer": {
              UUID id = getTargetUUID(args[0]);
              double amount = ((Number) args[1]).doubleValue();
              if (id == null) return createEconomyResponse(econRespClass, respTypeClass, amount, 0.0D, "FAILURE", "Player not found");
              plugin.getDatabaseManager().removeBalance(id.toString(), amount);
              plugin.getDatabaseManager().createTransaction(id.toString(), "VAULT", amount);
              double balance = plugin.getDatabaseManager().getBalance(id.toString());
              return createEconomyResponse(econRespClass, respTypeClass, amount, balance, "SUCCESS", "");
            }
            case "depositPlayer": {
              UUID id = getTargetUUID(args[0]);
              double amount = ((Number) args[1]).doubleValue();
              if (id == null) return createEconomyResponse(econRespClass, respTypeClass, amount, 0.0D, "FAILURE", "Player not found");
              plugin.getDatabaseManager().addBalance(id.toString(), amount);
              plugin.getDatabaseManager().createTransaction("VAULT", id.toString(), amount);
              double balance = plugin.getDatabaseManager().getBalance(id.toString());
              return createEconomyResponse(econRespClass, respTypeClass, amount, balance, "SUCCESS", "");
            }
            default:
              Class<?> returnType = method.getReturnType();
              if (returnType == boolean.class || returnType == Boolean.class) return false;
              if (returnType == double.class || returnType == Double.class) return 0.0D;
              if (returnType == int.class || returnType == Integer.class) return 0;
              return null;
          }
        } catch (Throwable t) {
          t.printStackTrace();
          throw t;
        }
      });

      // Register the proxy as the Economy provider
      sm.register((Class) econClass, proxy, this.plugin, ServicePriority.High);

    } catch (ClassNotFoundException e) {
      // Vault not installed - do nothing
      plugin.getLogger().info("Vault not found, skipping Vault integration.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private UUID getTargetUUID(Object arg) {
    if (arg instanceof OfflinePlayer) {
      return ((OfflinePlayer) arg).getUniqueId();
    } else if (arg instanceof String) {
      return plugin.getServer().getOfflinePlayer((String) arg).getUniqueId();
    }
    return null;
  }

  private Object createEconomyResponse(Class<?> econRespClass, Class<?> respTypeClass, double amount, double balance, String respName, String errorMessage) throws Exception {
    Object respType = Enum.valueOf((Class) respTypeClass, respName);
    Constructor<?> ctor = econRespClass.getConstructor(double.class, double.class, respTypeClass, String.class);
    return ctor.newInstance(amount, balance, respType, errorMessage);
  }
}
