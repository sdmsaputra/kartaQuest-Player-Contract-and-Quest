package com.playercontract.listeners;

import com.playercontract.PlayerContract;
import com.playercontract.data.Contract;
import com.playercontract.gui.ContractGUIHolder;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import com.playercontract.gui.PlayerInventoryGUIHolder;

import java.util.UUID;

public class GUIListener implements Listener {

    private final PlayerContract plugin;

    public GUIListener(PlayerContract plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof ContractGUIHolder holder) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;

            UUID contractId = holder.getContractIdAtSlot(event.getRawSlot());
            if (contractId == null) {
                // Handle navigation clicks
                if (event.getRawSlot() == 45 && event.getCurrentItem() != null) { // Previous Page
                    new com.playercontract.gui.ContractGUI(plugin, player, holder.getPage() - 1).open();
                } else if (event.getRawSlot() == 53 && event.getCurrentItem() != null) { // Next Page
                    new com.playercontract.gui.ContractGUI(plugin, player, holder.getPage() + 1).open();
                }
                return;
            }

            Contract contract = plugin.getContractManager().getContract(contractId);
            if (contract == null) return; // Should not happen

            // Prevent creator from accepting their own contract
            if (player.getUniqueId().equals(contract.creatorUuid())) {
                player.sendMessage(plugin.getConfigManager().getMessage("cannot-accept-own"));
                return;
            }

            if (plugin.getContractManager().hasActiveContract(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("already-has-contract"));
                return;
            }

            plugin.getContractManager().acceptContract(contractId, player.getUniqueId());
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessage("contract-accepted",
                    Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                    Placeholder.unparsed("item", contract.itemType().name())
            ));
        } else if (inventory.getHolder() instanceof PlayerInventoryGUIHolder holder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            UUID contractId = holder.getContractIdAtSlot(event.getRawSlot());
            if (contractId == null) return;

            Contract contract = plugin.getContractManager().getContract(contractId);
            if (contract == null || contract.status() != Contract.ContractStatus.COMPLETED_UNCLAIMED) {
                // Item is not a completed contract, do nothing.
                return;
            }

            // This is a completed contract, let the player claim it.
            // Note: The original request was to claim the *items*, but the flow seems to be that the worker gets the *money*.
            // I will implement it so the worker gets the money reward. The creator of the contract can get the items using /kontrak claim.
            plugin.getEconomyManager().depositPlayer(player, contract.reward());
            plugin.getReputationManager().addReputation(player.getUniqueId(), 1);
            plugin.getReputationManager().incrementCompletedContracts(player.getUniqueId());

            // The contract is now fully complete and can be removed.
            plugin.getContractManager().removeContract(contractId);

            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessage("reward-claimed", player,
                    Placeholder.unparsed("reward", String.format("%,.2f", contract.reward()))
            ));
        }
    }
}
