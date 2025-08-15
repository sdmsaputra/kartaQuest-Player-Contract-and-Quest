package com.playercontract.gui;

import com.playercontract.PlayerContract;
import com.playercontract.data.Contract;
import com.playercontract.utils.TimeParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerInventoryGUI {

    private final PlayerContract plugin;
    private final Player player;

    public PlayerInventoryGUI(PlayerContract plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        PlayerInventoryGUIHolder holder = new PlayerInventoryGUIHolder();
        Component title = plugin.getConfigManager().getMessage("inventory-gui-title", player, false);
        Inventory gui = Bukkit.createInventory(holder, 54, title);

        List<Contract> inProgressContracts = plugin.getContractManager().getInProgressContracts(player.getUniqueId());
        List<Contract> completedContracts = plugin.getContractManager().getCompletedContracts(player.getUniqueId());

        // Populate In-Progress Contracts
        for (int i = 0; i < inProgressContracts.size(); i++) {
            if (i >= 27) break; // Max 3 rows for in-progress
            Contract contract = inProgressContracts.get(i);
            gui.setItem(i, createInProgressItem(contract));
            holder.setContractAtSlot(i, contract.contractId());
        }

        // Populate Completed Contracts
        // Starting from slot 36 to leave a gap
        for (int i = 0; i < completedContracts.size(); i++) {
            int slot = 36 + i;
            if (slot >= 54) break; // Max 2 rows for completed
            Contract contract = completedContracts.get(i);
            gui.setItem(slot, createCompletedItem(contract));
            holder.setContractAtSlot(slot, contract.contractId());
        }

        // Add a separator
        ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = separator.getItemMeta();
        meta.displayName(Component.text(" "));
        separator.setItemMeta(meta);
        for (int i = 27; i < 36; i++) {
            gui.setItem(i, separator);
        }

        player.openInventory(gui);
    }

    private ItemStack createInProgressItem(Contract contract) {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(plugin.getConfigManager().getMessage("inventory-gui-in-progress-item-name", player, false,
                Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                Placeholder.unparsed("item", contract.itemType().name())
        ));

        List<String> loreLines = plugin.getConfig().getStringList("messages.inventory-gui-in-progress-item-lore");
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(plugin.getConfigManager().parse(line, player,
                    Placeholder.unparsed("reward", String.format("%,.2f", contract.reward())),
                    Placeholder.unparsed("creator", contract.creatorName()),
                    Placeholder.unparsed("time_accepted", TimeParser.formatTimeElapsed(contract.acceptedTimestamp()))
            ));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCompletedItem(Contract contract) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(plugin.getConfigManager().getMessage("inventory-gui-completed-item-name", player, false,
                Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                Placeholder.unparsed("item", contract.itemType().name())
        ));

        List<String> loreLines = plugin.getConfig().getStringList("messages.inventory-gui-completed-item-lore");
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(plugin.getConfigManager().parse(line, player,
                    Placeholder.unparsed("reward", String.format("%,.2f", contract.reward())),
                    Placeholder.unparsed("creator", contract.creatorName()),
                    Placeholder.unparsed("time_completed", TimeParser.formatTimeElapsed(contract.completedTimestamp()))
            ));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
