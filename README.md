# KartaPlayerContract

A modern, GUI-based player contract system for Spigot/Paper/Folia servers.

## Features

- **Player-Made Contracts**: Players can create contracts requesting specific items from other players.
- **Escrow System**: Contract rewards (money via Vault or items) are held securely in escrow until the contract is completed.
- **GUI-Based Workflow**: Most interactions are handled through intuitive graphical menus.
- **Command-Based Shortcuts**: All major actions can also be performed via commands with full tab-completion.
- **Delivery System**: Contractors deliver items to a virtual inbox for the contract owner to accept or reject.
- **Claim Box**: A safe place for players to retrieve returned items from cancelled or rejected contracts.
- **Persistence**: All data is saved in a local SQLite database.
- **Async & Folia-Safe**: All database and I/O operations are performed asynchronously to prevent server lag.

## Commands

The main command is `/contract`. It has the following subcommands:

- `/contract`: Opens the main contract menu.
- `/contract create`: Opens the GUI wizard to create a new contract.
- `/contract create item <MATERIAL> <amount> reward money <value>`: Example of the quick-create command.
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

## How to Test

1.  **Create a Contract (GUI)**:
    - Run `/contract create`.
    - Follow the multi-step wizard to define the requested item, quantity, and reward (either money or items).
    - Confirm the creation.
2.  **Take the Contract**:
    - As a different player, run `/contract list`.
    - Find the contract you created and click to view/take it. Or use `/contract take <id>`.
3.  **Deliver Items**:
    - As the second player, get the required items.
    - Run `/contract deliver <id>` to open the delivery GUI.
    - Place the items in the staging area and confirm.
4.  **Accept Delivery**:
    - As the original player, run `/contract inbox`.
    - You should see the delivery. Click it to open the review menu.
    - Click "Accept". The items will be transferred to you and the reward will be released to the contractor.

This covers the primary workflow of the plugin.
