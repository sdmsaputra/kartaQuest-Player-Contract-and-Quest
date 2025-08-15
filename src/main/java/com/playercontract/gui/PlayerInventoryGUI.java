package com.playercontract.gui;

import com.playercontract.PlayerContract;
import com.playercontract.data.Contract;
import com.playercontract.managers.ConfigManager;
import com.playercontract.utils.TimeParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerInventoryGUI {

    private final PlayerContract plugin;
    private final Player player;
    private final ConfigManager configManager;

    public PlayerInventoryGUI(PlayerContract plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.configManager = plugin.getConfigManager();
    }

    public void open() {
        PlayerInventoryGUIHolder holder = new PlayerInventoryGUIHolder();
        Component title = configManager.getMessage("player-inv-gui-title", player, false);
        Inventory gui = Bukkit.createInventory(holder, 54, title);

        List<Contract> createdContracts = plugin.getContractManager().getContractsByCreator(player.getUniqueId());

        for (int i = 0; i < createdContracts.size(); i++) {
            if (i >= 54) break; // Max inventory size
            Contract contract = createdContracts.get(i);
            gui.setItem(i, createCreatedContractItem(contract));
            holder.setContractAtSlot(i, contract.contractId());
        }

        player.openInventory(gui);
    }

    private ItemStack createCreatedContractItem(Contract contract) {
        ItemStack item;
        ItemMeta meta;
        List<Component> lore = new ArrayList<>();

        // Common lore parts
        lore.add(Component.empty());
        lore.add(configManager.getMessage("player-inv-gui-lore-item", player, false,
                Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                Placeholder.unparsed("item", contract.itemType().name())
        ));
        lore.add(configManager.getMessage("player-inv-gui-lore-reward", player, false,
                Placeholder.unparsed("reward", String.format("%,.2f", contract.reward()))
        ));

        switch (contract.status()) {
            case AVAILABLE:
                item = new ItemStack(Material.BOOK);
                meta = item.getItemMeta();
                meta.displayName(configManager.getMessage("player-inv-gui-status-available-title", player, false));
                lore.add(configManager.getMessage("player-inv-gui-status-available-lore", player, false));
                break;

            case IN_PROGRESS:
                item = new ItemStack(Material.WRITABLE_BOOK);
                meta = item.getItemMeta();
                meta.displayName(configManager.getMessage("player-inv-gui-status-in-progress-title", player, false));
                OfflinePlayer assignee = Bukkit.getOfflinePlayer(contract.assigneeUuid());
                String assigneeName = assignee.getName() != null ? assignee.getName() : configManager.getRawMessage("player-inv-gui-lore-unknown-player");
                lore.add(configManager.getMessage("player-inv-gui-lore-accepted-by", player, false,
                        Placeholder.unparsed("player", assigneeName)
                ));
                lore.add(configManager.getMessage("player-inv-gui-lore-accepted-at", player, false,
                        Placeholder.unparsed("time", TimeParser.formatTimeElapsed(contract.acceptedTimestamp()))
                ));
                break;

            case COMPLETED_UNCLAIMED:
                item = new ItemStack(Material.ENCHANTED_BOOK);
                meta = item.getItemMeta();
                meta.displayName(configManager.getMessage("player-inv-gui-status-completed-title", player, false));
                OfflinePlayer completer = Bukkit.getOfflinePlayer(contract.assigneeUuid());
                String completerName = completer.getName() != null ? completer.getName() : configManager.getRawMessage("player-inv-gui-lore-unknown-player");
                lore.add(configManager.getMessage("player-inv-gui-lore-completed-by", player, false,
                        Placeholder.unparsed("player", completerName)
                ));
                lore.add(configManager.getMessage("player-inv-gui-lore-completed-at", player, false,
                        Placeholder.unparsed("time", TimeParser.formatTimeElapsed(contract.completedTimestamp()))
                ));
                lore.add(Component.empty());
                lore.add(configManager.getMessage("player-inv-gui-lore-claim-items", player, false));
                break;

            case EXPIRED:
                item = new ItemStack(Material.BARRIER);
                meta = item.getItemMeta();
                meta.displayName(configManager.getMessage("player-inv-gui-status-expired-title", player, false));
                lore.add(configManager.getMessage("player-inv-gui-status-expired-lore", player, false));
                break;

            default:
                item = new ItemStack(Material.STONE);
                meta = item.getItemMeta();
                meta.displayName(configManager.getMessage("player-inv-gui-status-unknown-title", player, false));
                break;
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
