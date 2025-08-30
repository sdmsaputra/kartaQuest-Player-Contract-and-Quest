# KartaPlayerContract

A modern, GUI-based player contract system for Spigot/Paper/Folia servers.

## Features

- **Player-Made Contracts**: Players can create contracts requesting specific items from other players.
- **Money-Based Economy**: All contracts use Vault for monetary rewards. Item-based rewards are not supported.
- **Two Ways to Create**:
    - **GUI-Based Creation**: An interactive, chat-based prompt system guides players through creating a contract.
    - **Command-Based Creation**: A quick command allows for fast contract creation without needing to navigate menus.
- **GUI-Based Workflow**: Most interactions are handled through intuitive graphical menus with full pagination.
- **Delivery System**: Contractors deliver items to a virtual inbox for the contract owner to accept or reject.
- **Claim Box**: A safe place for players to retrieve returned items from cancelled or rejected contracts.
- **Persistence**: All data is saved in a local SQLite database.
- **Async & Folia-Safe**: All database and I/O operations are performed asynchronously to prevent server lag.

## Commands

The main command is `/contract`. It has the following subcommands:

- `/contract`: Opens the main contract menu.
- `/contract create <item_name> <amount> <price>`: Quickly create a new contract.
  - `item_name`: The name of the Minecraft item (e.g., `diamond_sword`).
  - `amount`: The number of items you need.
  - `price`: The total amount you will pay the contractor.
- `/contract list`: Opens the list of available contracts.
- `/contract take <id>`: Take an open contract.
- `/contract deliver <id>`: Open the delivery GUI for a contract you have taken.
- `/contract inbox`: View deliveries waiting for your approval.
- `/contract claims`: Open your claim box to retrieve returned items.
- `/contract cancel <id>`: Cancel a contract you have created.
- `/contract history`: View your contract history.
- `/contract stats`: View your statistics.
- `/contract reload`: (Admin) Reloads the plugin's configuration files.

## Permissions

- `karta.contract.create`: Allows creating contracts. (default: true)
- `karta.contract.take`: Allows taking contracts. (default: true)
- `karta.contract.manage`: Allows managing own contracts (e.g., cancel). (default: true)
- `karta.contract.admin`: Allows admin commands like `/contract reload`. (default: op)

## How to Create a Contract

There are two methods to create a contract:

### 1. GUI Method (Recommended for new users)
- Run `/contract` to open the main menu.
- Click on the "Create Contract" button (the Anvil).
- The GUI will close, and you will be prompted in chat.
- Simply type your answers for the item name, amount, and price as requested.
- If you make a mistake, you can type `cancel` at any time.
- Once complete, the contract will be created, and the main menu will re-open.

### 2. Command Method (For advanced users)
- Use the command `/contract create <item_name> <amount> <price>`.
- For example: `/contract create cobblestone 256 100.50` will create a contract asking for 256 cobblestone for a price of $100.50.

This covers the primary workflow of the plugin.
