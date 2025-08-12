package com.kartaquest.commands;

import com.kartaquest.KartaQuest;
import com.kartaquest.data.Contract;
import com.kartaquest.gui.ContractGUI;
import com.kartaquest.utils.TimeParser;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KartaQuestCommand implements CommandExecutor, TabCompleter {
    private final KartaQuest plugin;

    public KartaQuestCommand(KartaQuest plugin) {
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
                handleAdminCommand(player, args);
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
            List<String> subCommands = List.of("create", "status", "complete", "cancel", "claim");
            List<String> completions = new ArrayList<>();
            for (String s : subCommands) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
            if (sender.hasPermission("kartaquest.admin")) {
                if ("admin".startsWith(args[0].toLowerCase())) {
                    completions.add("admin");
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 4 || args.length > 5) {
            player.sendMessage(plugin.getConfigManager().getMessage("creation-usage", player));
            return;
        }

        int maxContracts = plugin.getConfigManager().getMaxContractsPerPlayer();
        long currentContracts = plugin.getContractManager().getActiveContracts().values().stream()
                .filter(c -> c.getCreatorUuid().equals(player.getUniqueId())).count();
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

    private void handleStatusCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract", player));
            return;
        }

        player.sendMessage(plugin.getConfigManager().getMessage("contract-status", player,
                Placeholder.unparsed("amount", String.valueOf(contract.getItemAmount())),
                Placeholder.unparsed("item", contract.getItemType().name()),
                Placeholder.unparsed("reward", String.format("%,.2f", contract.getReward())),
                Placeholder.unparsed("creator", contract.getCreatorName())
        ));
    }

    private void handleCompleteCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract", player));
            return;
        }

        ItemStack requiredItem = new ItemStack(contract.getItemType());
        if (!player.getInventory().containsAtLeast(requiredItem, contract.getItemAmount())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-enough-items", player,
                    Placeholder.unparsed("amount", String.valueOf(contract.getItemAmount())),
                    Placeholder.unparsed("item", contract.getItemType().name())
            ));
            return;
        }

        ItemStack itemToStore = null;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(requiredItem)) {
                itemToStore = inventoryItem.clone();
                break;
            }
        }

        if (itemToStore == null) {
             player.sendMessage("Could not find the required items in your inventory. This is a bug, please report it.");
             return;
        }

        ItemStack itemsToRemove = itemToStore.clone();
        itemsToRemove.setAmount(contract.getItemAmount());
        player.getInventory().removeItem(itemsToRemove);

        itemToStore.setAmount(contract.getItemAmount());

        plugin.getEconomyManager().depositPlayer(player, contract.getReward());
        plugin.getReputationManager().addReputation(player.getUniqueId(), 1);
        plugin.getReputationManager().incrementCompletedContracts(player.getUniqueId());
        plugin.getContractManager().completeContract(contract.getContractId(), itemToStore);

        player.sendMessage(plugin.getConfigManager().getMessage("contract-completed", player,
                Placeholder.unparsed("reward", String.format("%,.2f", contract.getReward()))
        ));
    }

    private void handleCancelCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract", player));
            return;
        }

        plugin.getContractManager().cancelContract(contract.getContractId());
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
            player.getInventory().addItem(contract.getCompletedItem());
            plugin.getContractManager().removeContract(contract.getContractId());
            player.sendMessage(plugin.getConfigManager().getMessage("claimed-items", player,
                    Placeholder.unparsed("amount", String.valueOf(contract.getCompletedItem().getAmount())),
                    Placeholder.unparsed("item", contract.getCompletedItem().getType().name())
            ));
        }
    }

    private void handleAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("kartaquest.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("unknown-command", player));
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
                    player.sendMessage("Usage: /kq admin delete <contract-id>");
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
}
