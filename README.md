# Karta PlayerContract

Karta PlayerContract is a modern Minecraft plugin that provides a complete, GUI-based system for players to create and fulfill contracts with each other. It's designed to be intuitive, easy to use, and a powerful tool for building a server economy.

## Features

- **Fully GUI-Based:** No more clunky commands. Everything is handled through a clean, interactive, and easy-to-understand graphical interface.
- **Main Menu:** A central hub (`/contract`) to access all features of the plugin.
- **Contract List:** Browse all available contracts, see what they require, and accept them with a single click.
- **Reward Inventory:** A personal inventory to see all your pending rewards from completed contracts. Claim them whenever you're ready!
- **Contract History:** Look back on all the contracts you've accepted, completed, or failed.
- **Player Statistics:** A dedicated screen to view your personal contract-related stats, like completion rate, total earnings, and more.
- **Admin Features:** Simple commands for admins to reload the plugin or open the contract menu for other players.
- **Highly Configurable:** Customize all GUIs, messages, and sounds to fit your server's theme.
- **PlaceholderAPI Support:** (Coming Soon) Integrate contract data into other plugins.
- **Modern & Performant:** Built with Kotlin and asynchronous database operations to ensure minimal impact on server performance.

## Commands

The command system is designed for simplicity and ease of use.

**Main Command:** `/contract` (Aliases: `/playercontract`, `/pc`)

| Subcommand | Description | Permission |
| --- | --- | --- |
| (no subcommand) | Opens the main contract menu GUI. | `karta.contract.open` |
| `list` | Opens the Contract List GUI directly. | `karta.contract.open` |
| `inventory` | Opens your personal Reward Inventory. | `karta.contract.inventory` |
| `history` | Opens your Contract History. | `karta.contract.history` |
| `stats` | Opens your Player Statistics. | `karta.contract.stats` |
| `help` | Shows a list of all available commands. | (none) |
| `reload` | Reloads the plugin configuration. | `karta.contract.admin.reload` |
| `open <player>` | Opens the main contract menu for another player. | `karta.contract.admin.open` |

## Permissions

Permissions are straightforward and grant access to the features players need.

| Permission | Description | Default |
| --- | --- | --- |
| `karta.contract.user` | A parent permission that grants all user-level permissions. | `true` |
| `karta.contract.open` | Allows opening the main contract menu and contract list. | `true` |
| `karta.contract.inventory` | Allows opening the reward inventory. | `true` |
| `karta.contract.history` | Allows opening the contract history. | `true` |
| `karta.contract.stats` | Allows opening the player statistics. | `true` |
| `karta.contract.admin` | A parent permission for all admin commands. | `op` |
| `karta.contract.admin.reload` | Allows reloading the plugin configuration. | `op` |
| `karta.contract.admin.open` | Allows opening the contract menu for other players. | `op` |

## Configuration

The plugin is highly configurable through the files located in the `/plugins/KartaPlayerContract/` directory.

- `config.yml`: Main plugin configuration.
- `gui.yml`: Configure all aspects of the GUIs, including titles, sizes, items, and slots.
- `message.yml`: Customize all messages and sounds used by the plugin.
