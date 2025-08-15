package com.playercontract.commands;

import com.playercontract.PlayerContract;
import com.playercontract.data.Contract;
import com.playercontract.gui.ContractGUI;
import com.playercontract.gui.PlayerInventoryGUI;
import com.playercontract.utils.TimeParser;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerContractCommand implements CommandExecutor, TabCompleter {
    // ... (existing code up to handleCreateCommand)
    private final PlayerContract plugin;

    public PlayerContractCommand(PlayerContract plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only-command"));
            return true;
        }

        if (args.length == 0) {
            new ContractGUI(plugin, player, 0).open();
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "inv":
            case "inventory":
                new PlayerInventoryGUI(plugin, player).open();
                break;
            case "create":
                handleCreateCommand(player, args);
                break;
            case "status":
                handleStatusCommand(player);
                break;
            case "complete":
                handleCompleteCommand(player);
                break;
            case "cancel":
                handleCancelCommand(player);
                break;
            case "claim":
                handleClaimCommand(player);
                break;
            case "admin":
                handleAdminCommand(player, args, label);
                break;
            case "rep":
            case "reputation":
                handleReputationCommand(sender, args, label);
                break;
            case "reload":
                if (!sender.hasPermission(plugin.getConfigManager().getAdminPermission())) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("unknown-command", (Player) sender));
                    return true;
                }
                plugin.getConfigManager().reloadConfig();
                sender.sendMessage(plugin.getConfigManager().getMessage("config-reloaded", (Player) sender));
                break;
            default:
                player.sendMessage(plugin.getConfigManager().getMessage("unknown-command", player));
                break;
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(List.of("create", "status", "complete", "cancel", "claim", "inv", "inventory", "rep", "reputation"));
            List<String> completions = new ArrayList<>();
            for (String s : subCommands) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
            if (sender.hasPermission(plugin.getConfigManager().getAdminPermission())) {
                if ("admin".startsWith(args[0].toLowerCase())) {
                    completions.add("admin");
                }
                if ("reload".startsWith(args[0].toLowerCase())) {
                    completions.add("reload");
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 4 || args.length > 5) {
            player.sendMessage(plugin.getConfigManager().getMessage("creation-usage", player)); // Should update usage message
            return;
        }

        // Max contract check
        int maxContracts = plugin.getConfigManager().getMaxContractsPerPlayer();
        long currentContracts = plugin.getContractManager().getActiveContracts().values().stream()
                .filter(c -> c.creatorUuid().equals(player.getUniqueId())).count();
        if (currentContracts >= maxContracts) {
            player.sendMessage(plugin.getConfigManager().getMessage("max-contracts-reached", player, Placeholder.unparsed("max", String.valueOf(maxContracts))));
            return;
        }

        Material material;
        try {
            material = Material.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-item", player, Placeholder.unparsed("item", args[1])));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", player));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", player));
            return;
        }

        double price;
        try {
            price = Double.parseDouble(args[3]);
            if (price <= 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-price", player));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-price", player));
            return;
        }

        long timeLimit = 0;
        if (args.length == 5) {
            timeLimit = TimeParser.parseTime(args[4]);
            if (timeLimit == 0) {
                // TimeParser returns 0 if format is invalid
                player.sendMessage("Invalid time format. Use d, h, m, s. Example: 7d, 12h, 30m");
                return;
            }
        }

        double tax = price * (plugin.getConfigManager().getContractCreationTaxPercent() / 100.0);
        double totalCost = price + tax;

        if (!plugin.getEconomyManager().has(player, totalCost)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds", player,
                    Placeholder.unparsed("price", String.format("%,.2f", price)),
                    Placeholder.unparsed("tax", String.format("%,.2f", tax))
            ));
            return;
        }

        EconomyResponse response = plugin.getEconomyManager().withdrawPlayer(player, totalCost);
        if (!response.transactionSuccess()) {
            player.sendMessage("An unexpected error occurred during the transaction.");
            return;
        }

        plugin.getContractManager().createContract(player.getUniqueId(), player.getName(), material, amount, price, timeLimit);
        player.sendMessage(plugin.getConfigManager().getMessage("contract-created", player,
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.unparsed("item", material.name()),
                Placeholder.unparsed("price", String.format("%,.2f", price))
        ));
    }

    // ... other handle methods
    private void handleStatusCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract", player));
            return;
        }

        player.sendMessage(plugin.getConfigManager().getMessage("contract-status", player,
                Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                Placeholder.unparsed("item", contract.itemType().name()),
                Placeholder.unparsed("reward", String.format("%,.2f", contract.reward())),
                Placeholder.unparsed("creator", contract.creatorName())
        ));
    }
    private void handleCompleteCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract", player));
            return;
        }

        if (!player.getInventory().containsAtLeast(new ItemStack(contract.itemType()), contract.itemAmount())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-enough-items", player,
                    Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                    Placeholder.unparsed("item", contract.itemType().name())
            ));
            return;
        }

        player.getInventory().removeItem(new ItemStack(contract.itemType(), contract.itemAmount()));
        plugin.getEconomyManager().depositPlayer(player, contract.reward());
        plugin.getReputationManager().addReputation(player.getUniqueId(), 1);
        plugin.getReputationManager().incrementCompletedContracts(player.getUniqueId());
        plugin.getContractManager().completeContract(contract.contractId());

        player.sendMessage(plugin.getConfigManager().getMessage("contract-completed", player,
                Placeholder.unparsed("reward", String.format("%,.2f", contract.reward()))
        ));
    }

    private void handleCancelCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract", player));
            return;
        }

        plugin.getContractManager().cancelContract(contract.contractId());
        plugin.getReputationManager().removeReputation(player.getUniqueId(), 1);

        player.sendMessage(plugin.getConfigManager().getMessage("contract-cancelled", player));
    }

    private void handleClaimCommand(Player player) {
        List<Contract> claimable = plugin.getContractManager().getClaimableContracts(player.getUniqueId());
        if (claimable.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-items-to-claim", player));
            return;
        }

        for (Contract contract : claimable) {
            ItemStack itemsToClaim = new ItemStack(contract.itemType(), contract.itemAmount());
            if (inventoryHasSpace(player.getInventory(), itemsToClaim)) {
                player.getInventory().addItem(itemsToClaim);
                plugin.getContractManager().removeContract(contract.contractId());
                player.sendMessage(plugin.getConfigManager().getMessage("claimed-items", player,
                        Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                        Placeholder.unparsed("item", contract.itemType().name())
                ));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("inventory-full-claim",
                        Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                        Placeholder.unparsed("item", contract.itemType().name())
                ));
                // Stop processing claims if one fails due to lack of space
                break;
            }
        }
    }

    private boolean inventoryHasSpace(Inventory inventory, ItemStack item) {
        int amountNeeded = item.getAmount();

        // Check for existing stacks that can be filled
        for (ItemStack slot : inventory.getStorageContents()) {
            if (slot != null && slot.isSimilar(item)) {
                amountNeeded -= (slot.getMaxStackSize() - slot.getAmount());
            }
        }
        if (amountNeeded <= 0) return true;

        // Check for empty slots
        for (ItemStack slot : inventory.getStorageContents()) {
            if (slot == null || slot.getType() == Material.AIR) {
                amountNeeded -= item.getMaxStackSize();
            }
        }

        return amountNeeded <= 0;
    }

    private void handleAdminCommand(Player player, String[] args, String label) {
        if (!player.hasPermission(plugin.getConfigManager().getAdminPermission())) {
            player.sendMessage(plugin.getConfigManager().getMessage("unknown-command", player)); // Hide admin command
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("admin-help", player));
            return;
        }

        String adminSubCommand = args[1].toLowerCase();
        switch (adminSubCommand) {
            case "reload":
                plugin.getConfigManager().reloadConfig();
                player.sendMessage(plugin.getConfigManager().getMessage("config-reloaded", player));
                break;
            case "delete":
                if (args.length < 3) {
                        player.sendMessage("Usage: /" + label + " admin delete <contract-id>");
                    return;
                }
                try {
                    UUID contractId = UUID.fromString(args[2]);
                    if (plugin.getContractManager().getContract(contractId) == null) {
                        player.sendMessage(plugin.getConfigManager().getMessage("contract-not-found", player, Placeholder.unparsed("id", args[2])));
                    } else {
                        plugin.getContractManager().removeContract(contractId);
                        player.sendMessage(plugin.getConfigManager().getMessage("contract-deleted", player, Placeholder.unparsed("id", args[2])));
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("contract-not-found", player, Placeholder.unparsed("id", args[2])));
                }
                break;
            default:
                player.sendMessage(plugin.getConfigManager().getMessage("admin-help", player));
                break;
        }
    }

    private void handleReputationCommand(CommandSender sender, String[] args, String label) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-only-command"));
                return;
            }
            int reputation = plugin.getReputationManager().getReputation(player.getUniqueId());
            player.sendMessage(plugin.getConfigManager().getMessage("reputation-check-self", player,
                    Placeholder.unparsed("reputation", String.valueOf(reputation))
            ));
            return;
        }

        if (args.length == 2) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            OfflinePlayer viewer = (sender instanceof Player) ? (Player) sender : null;

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found", viewer));
                return;
            }
            int reputation = plugin.getReputationManager().getReputation(target.getUniqueId());
            sender.sendMessage(plugin.getConfigManager().getMessage("reputation-check-other", viewer,
                    Placeholder.unparsed("player", target.getName()),
                    Placeholder.unparsed("reputation", String.valueOf(reputation))
            ));
            return;
        }

        sender.sendMessage("Usage: /" + label + " reputation [player]"); // This can be a configurable message too
    }
}
