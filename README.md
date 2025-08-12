# KartaQuest - Player Contract and Quest Plugin

A social and economic plugin that formalizes interactions between players through a system of contracts and reputation, encouraging collaboration and role specialization.

## Features

- **Player-Driven Contracts:** Players can create contracts to request items from other players.
- **Contract Board GUI:** An easy-to-use GUI (`/kq`) to view and accept available contracts.
- **Reputation System:** Gain reputation by completing contracts and lose it by canceling them.
- **Vault Integration:** Uses Vault for all economic transactions, including contract rewards and creation taxes.
- **Time-Limited Contracts:** Set an optional time limit for contracts to be completed.
- **Admin Commands:** Admins can manage the plugin by reloading the configuration and deleting contracts.

## Commands

Here is a list of all the commands available in KartaQuest.

### Main Command: `/kartaquest`
Aliases: `/quest`, `/quests`, `/kq`, `/kontrak`

| Subcommand                               | Description                                                                                               |
| ---------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| (no subcommand)                          | Opens the main contract board GUI to view and accept contracts.                                           |
| `create <item> <amount> <price> [time]` | Creates a new contract. Example: `/kq create DIAMOND 16 1000 7d`                                          |
| `status`                                 | Checks the status of your currently accepted contract.                                                    |
| `complete`                               | Turns in the required items to complete your contract and receive the reward.                             |
| `cancel`                                 | Cancels your current contract. This will result in a loss of reputation.                                  |
| `claim`                                  | Claims the items from a contract you created that another player has completed.                           |
| `admin reload`                           | Reloads the plugin's configuration file.                                                                  |
| `admin delete <contract-id>`             | Deletes a contract from the board using its unique ID.                                                    |

### Reputation Command: `/reputations`
Aliases: `/rep`, `/reputasi`

| Subcommand      | Description                                |
| --------------- | ------------------------------------------ |
| (no subcommand) | Checks your own reputation score.          |
| `<player>`      | Checks the reputation score of another player. |

## Permissions

| Permission          | Description                                    | Default |
| ------------------- | ---------------------------------------------- | ------- |
| `kartaquest.admin`  | Grants access to the `/kartaquest admin` commands. | Op      |
