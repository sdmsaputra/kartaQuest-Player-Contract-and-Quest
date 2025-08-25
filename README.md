# Karta PlayerContract - v2.2.0

Karta PlayerContract is a Minecraft plugin that enhances player interaction by creating a formalized system of contracts, tasks, and reputation. It allows players to post jobs for others to complete, fostering a dynamic and player-driven economy.

## Features

- **Player-Driven Contracts:** Players can create contracts to request items from other players for a fee.
- **Contract Board GUI:** A main GUI (`/contract`) shows all available contracts for players to accept.
- **Personal Inventory GUI:** A new GUI (`/contract inv`) allows players to view their accepted and completed contracts, see who created them, and track how long they've been active.
- **Cancel Your Own Contracts:** Easily cancel a contract you've created directly from the personal inventory GUI (if it hasn't been accepted yet).
- **Click-to-Claim Rewards:** Once a contract is completed, the worker can simply click the item in their personal inventory GUI to receive their payment.
- **Reputation System:** Gain reputation by completing contracts and lose it by canceling them. Check your own or others' reputation with `/contract reputation`.
- **Visual Improvements:** The in-game GUI now has a cleaner look with non-italicized text and better-organized item details.
- **Tab-Completion for Items:** When creating a contract, the item argument will now suggest available items.
- **Command Guidance:** The `/contract create` command now provides clear guidance on how to correctly specify the price and duration for a new contract.
- **Vault Integration:** Uses Vault for all economic transactions.
- **Time-Limited Contracts:** Optionally set a time limit for contracts.
- **Admin Commands:** Admins can reload the configuration and manage contracts.

## Commands

Here is a list of all the commands available in Karta PlayerContract.

### Main Command: `/playercontract`
Aliases: `/contract`

| Subcommand | Description |
| --- | --- |
| (no subcommand) | Opens the main contract board GUI. |
| `inv` or `inventory` | Opens your personal contract inventory to see your active and completed contracts. You can also cancel contracts you have created from this menu. |
| `create <item> <amount> <price> [time]` | Creates a new contract. <br> - `<price>`: The total amount you will pay the player who completes the contract. <br> - `[time_limit]` is an optional duration for the contract (e.g., 7d, 12h, 30m). |
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
| `playercontract.admin` | Grants access to the `/contract admin` commands. | Op |
