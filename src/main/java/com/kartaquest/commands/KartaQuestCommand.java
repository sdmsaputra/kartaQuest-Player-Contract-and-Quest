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
    // ... (existing code up to handleCreateCommand)
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
                player.sendMessage(plugin.getConfigManager().getMessage("unknown-command"));
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
            player.sendMessage(plugin.getConfigManager().getMessage("creation-usage")); // Should update usage message
            return;
        }

        // Max contract check
        int maxContracts = plugin.getConfigManager().getMaxContractsPerPlayer();
        long currentContracts = plugin.getContractManager().getActiveContracts().values().stream()
                .filter(c -> c.creatorUuid().equals(player.getUniqueId())).count();
        if (currentContracts >= maxContracts) {
            player.sendMessage(plugin.getConfigManager().getMessage("max-contracts-reached", Placeholder.unparsed("max", String.valueOf(maxContracts))));
            return;
        }

        Material material;
        try {
            material = Material.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-item", Placeholder.unparsed("item", args[1])));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
            return;
        }

        double price;
        try {
            price = Double.parseDouble(args[3]);
            if (price <= 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-price"));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-price"));
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
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds",
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
        player.sendMessage(plugin.getConfigManager().getMessage("contract-created",
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.unparsed("item", material.name()),
                Placeholder.unparsed("price", String.format("%,.2f", price))
        ));
    }

    // ... other handle methods
    private void handleStatusCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract"));
            return;
        }

        player.sendMessage(plugin.getConfigManager().getMessage("contract-status",
                Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                Placeholder.unparsed("item", contract.itemType().name()),
                Placeholder.unparsed("reward", String.format("%,.2f", contract.reward())),
                Placeholder.unparsed("creator", contract.creatorName())
        ));
    }
    private void handleCompleteCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract"));
            return;
        }

        if (!player.getInventory().containsAtLeast(new ItemStack(contract.itemType()), contract.itemAmount())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-enough-items",
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

        player.sendMessage(plugin.getConfigManager().getMessage("contract-completed",
                Placeholder.unparsed("reward", String.format("%,.2f", contract.reward()))
        ));
    }

    private void handleCancelCommand(Player player) {
        Contract contract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
        if (contract == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-contract"));
            return;
        }

        plugin.getContractManager().cancelContract(contract.contractId());
        plugin.getReputationManager().removeReputation(player.getUniqueId(), 1);

        player.sendMessage(plugin.getConfigManager().getMessage("contract-cancelled"));
    }

    private void handleClaimCommand(Player player) {
        List<Contract> claimable = plugin.getContractManager().getClaimableContracts(player.getUniqueId());
        if (claimable.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-items-to-claim"));
            return;
        }

        for (Contract contract : claimable) {
            player.getInventory().addItem(new ItemStack(contract.itemType(), contract.itemAmount()));
            plugin.getContractManager().removeContract(contract.contractId());
            player.sendMessage(plugin.getConfigManager().getMessage("claimed-items",
                    Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                    Placeholder.unparsed("item", contract.itemType().name())
            ));
        }
    }

    private void handleAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("kartaquest.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("unknown-command")); // Hide admin command
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("admin-help"));
            return;
        }

        String adminSubCommand = args[1].toLowerCase();
        switch (adminSubCommand) {
            case "reload":
                plugin.getConfigManager().reloadConfig();
                player.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
                break;
            case "delete":
                if (args.length < 3) {
                    player.sendMessage("Usage: /kq admin delete <contract-id>");
                    return;
                }
                try {
                    UUID contractId = UUID.fromString(args[2]);
                    if (plugin.getContractManager().getContract(contractId) == null) {
                        player.sendMessage(plugin.getConfigManager().getMessage("contract-not-found", Placeholder.unparsed("id", args[2])));
                    } else {
                        plugin.getContractManager().removeContract(contractId);
                        player.sendMessage(plugin.getConfigManager().getMessage("contract-deleted", Placeholder.unparsed("id", args[2])));
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("contract-not-found", Placeholder.unparsed("id", args[2])));
                }
                break;
            default:
                player.sendMessage(plugin.getConfigManager().getMessage("admin-help"));
                break;
        }
    }
}
