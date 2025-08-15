# Player Contract - v2.0.0 BETA

Player Contract is a Minecraft plugin that enhances player interaction by creating a formalized system of contracts, tasks, and reputation. It allows players to post jobs for others to complete, fostering a dynamic and player-driven economy.

## Features

- **Player-Driven Contracts:** Players can create contracts to request items from other players for a fee.
- **Contract Board GUI:** A main GUI (`/pc`) shows all available contracts for players to accept.
- **Personal Inventory GUI:** A new GUI (`/pc inv`) allows players to view their accepted and completed contracts, see who created them, and track how long they've been active.
- **Click-to-Claim Rewards:** Once a contract is completed, the worker can simply click the item in their personal inventory GUI to receive their payment.
- **Reputation System:** Gain reputation by completing contracts and lose it by canceling them. Check your own or others' reputation with `/pc reputation`.
- **Vault Integration:** Uses Vault for all economic transactions.
- **Time-Limited Contracts:** Optionally set a time limit for contracts.
- **Admin Commands:** Admins can reload the configuration and manage contracts.

## Commands

Here is a list of all the commands available in Player Contract.

### Main Command: `/playercontract`
Aliases: `/pc`, `/kontrak`

| Subcommand | Description |
| --- | --- |
| (no subcommand) | Opens the main contract board GUI. |
| `inv` or `inventory` | Opens your personal contract inventory to see your active and completed contracts. |
| `create <item> <amount> <price> [time]` | Creates a new contract. Example: `/pc create DIAMOND 16 1000 7d` |
| `status` | Checks the status of your currently accepted contract. |
| `complete` | Turns in the required items to complete your contract. |
| `cancel` | Cancels your current contract (incurs a reputation penalty). |
| `claim` | Claims the items from a contract you created that another player has completed. |
| `reputation [player]` | Checks your own or another player's reputation. |
| `admin reload` | Reloads the plugin's configuration file. |
| `admin delete <contract-id>` | Forcefully deletes a contract from the board. |

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `playercontract.admin` | Grants access to the `/pc admin` commands. | Op |
