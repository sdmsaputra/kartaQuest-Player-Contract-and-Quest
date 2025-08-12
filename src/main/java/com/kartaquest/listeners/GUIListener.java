package com.kartaquest.listeners;

import com.kartaquest.KartaQuest;
import com.kartaquest.data.Contract;
import com.kartaquest.gui.ContractGUIHolder;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class GUIListener implements Listener {

    private final KartaQuest plugin;

    public GUIListener(KartaQuest plugin) {
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
                if (event.getRawSlot() == 45) { // Previous Page
                    new com.kartaquest.gui.ContractGUI(plugin, player, holder.getPage() - 1).open();
                } else if (event.getRawSlot() == 53) { // Next Page
                    new com.kartaquest.gui.ContractGUI(plugin, player, holder.getPage() + 1).open();
                }
                return;
            }

            Contract contract = plugin.getContractManager().getContract(contractId);
            if (contract == null) return; // Should not happen

            // Prevent creator from accepting their own contract
            if (player.getUniqueId().equals(contract.getCreatorUuid())) {
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
                    Placeholder.unparsed("amount", String.valueOf(contract.getItemAmount())),
                    Placeholder.unparsed("item", contract.getItemType().name())
            ));
        }
    }
}
