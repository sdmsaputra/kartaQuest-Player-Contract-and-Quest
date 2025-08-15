package com.playercontract.expansion;

import com.playercontract.PlayerContract;
import com.playercontract.data.Contract;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerContractExpansion extends PlaceholderExpansion {

    private final PlayerContract plugin;

    public PlayerContractExpansion(PlayerContract plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playercontract";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MinekartaStudio";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params) {
            case "reputation":
                return String.valueOf(plugin.getReputationManager().getReputation(player.getUniqueId()));

            case "contracts_completed":
                return String.valueOf(plugin.getReputationManager().getCompletedContracts(player.getUniqueId()));

            case "contracts_created":
                long createdCount = plugin.getContractManager().getActiveContracts().values().stream()
                        .filter(c -> c.creatorUuid().equals(player.getUniqueId())).count();
                return String.valueOf(createdCount);

            case "active_contract_name":
                Contract activeContract = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
                if (activeContract != null) {
                    return "Collect " + activeContract.itemAmount() + " " + activeContract.itemType().name();
                }
                return "None";

            case "active_contract_reward":
                Contract activeContractReward = plugin.getContractManager().getContractByAssignee(player.getUniqueId());
                if (activeContractReward != null) {
                    return String.format("%,.2f", activeContractReward.reward());
                }
                return "0.00";

            default:
                return null;
        }
    }
}
