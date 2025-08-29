# 💰 EconomySystem

**EconomySystem** is a lightweight, no-dependency economy plugin designed for Minecraft servers that need a simple and fast digital currency system. Whether you're running a Survival, CityBuild, or SkyBlock server, this plugin gives you essential economy features without unnecessary bloat.

---

## ✨ Features

- 💵 Simple digital currency system (no items or banknotes)
- ⚙️ Fully configurable currency format and starting balance
- 🧾 Transaction logging (payments, admin changes, etc.)
- 💬 Basic player commands: `/pay`, `/balance`, `/balancetop`
- 🛠️ Admin commands to set, add, or remove player balance
- 📦 Local database support using **LiteSQL** (`.db` file)
- 🐬 Optional support for **MariaDB** if preferred
- 🛠 No dependencies – **Vault is NOT required**
- 🌐 Designed for performance and cross-version compatibility

---

## 🧑‍💼 Commands & Permissions

| Command | Description | Permission |
|--------|-------------|------------|
| `/balance [player]` | View your or another player's balance | `economysystem.command.balance` |
| `/pay <player> <amount>` | Send money to another player | `economysystem.command.pay` |
| `/balancetop` *(aliases: `/baltop`, `/topbalance`)* | Show top 10 richest players | `economysystem.command.balancetop` |
| `/economyadmin <set/add/remove> <player> <amount>` *(alias: `/ecoa`)* | Admin command to modify balances | `economysystem.command.economyadmin` |

---

## 🧩 Dependencies

None.  
EconomySystem runs fully standalone – no Vault, PlaceholderAPI, or other plugins required.

---

## ⚙️ Configuration

The plugin provides a clean and powerful configuration system. Example:

```yaml
economy:
    # The currency symbol
    currency-symbol: "$"
    # The currency name
    currency-name: "Dollar"
    # Amount of money players start with
    start-balance: 100.0

database:
    type: litesql # Options: litesql, mariadb
    mariadb:
        host: "localhost"
        port: 3306
        database: "economy"
        username: "user"
        password: ""
    litesql:
        file: "economy.db"
```

✅ By default, the plugin stores all player data in a local .db file using LiteSQL.

🔁 You can optionally configure MariaDB for better performance and scalability.

---

## 📦 Compatibility

EconomySystem is designed for broad compatibility and long-term stability across various Minecraft server types and versions.

### ✅ Supported Minecraft Versions
- **1.8.x** to **1.20.x+**
- Actively tested on latest Paper/Purpur builds

### ✅ Supported Server Software
- **Paper**
- **Spigot**
- **Purpur**
- **Bukkit**
- **Any fork compatible with Bukkit API**

### 🧩 Plugin Dependencies
- ❌ **None!**
- No Vault, PlaceholderAPI, or other plugins required

### 🛢️ Database Support
| Type     | Description                           | Recommended For                   |
|----------|---------------------------------------|------------------------------------|
| `litesql` | Lightweight local `.db` file (default) | Small to medium servers            |
| `mariadb` | External SQL-based database           | Larger networks or multi-server setups |

> 💡 You can switch between databases at any time via the `config.yml`.

---

### 🧪 Performance & Integration
- Minimal memory and CPU usage
- Asynchronous data handling
- Fast load times, even with thousands of player records

---
