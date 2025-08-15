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

public class ContractGUI {

    private final PlayerContract plugin;
    private final Player player;
    private int page;
    private static final int CONTRACTS_PER_PAGE = 45; // 9x5

    public ContractGUI(PlayerContract plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
    }

    public void open() {
        List<Contract> availableContracts = plugin.getContractManager().getAvailableContracts();
        ContractGUIHolder holder = new ContractGUIHolder(page);

        int totalPages = Math.max(1, (int) Math.ceil((double) availableContracts.size() / CONTRACTS_PER_PAGE));
        Component title = plugin.getConfigManager().getMessage("gui-title", player, false,
                Placeholder.unparsed("page", String.valueOf(page + 1)),
                Placeholder.unparsed("total_pages", String.valueOf(totalPages)));

        Inventory gui = Bukkit.createInventory(holder, 54, title);

        int startIndex = page * CONTRACTS_PER_PAGE;
        int endIndex = Math.min(startIndex + CONTRACTS_PER_PAGE, availableContracts.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = i % CONTRACTS_PER_PAGE;
            Contract contract = availableContracts.get(i);
            gui.setItem(slot, createContractItem(contract));
            holder.setContractAtSlot(slot, contract.contractId());
        }

        // Add navigation items
        if (page > 0) {
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = previous.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page")); // This can be made configurable too
            previous.setItemMeta(prevMeta);
            gui.setItem(45, previous); // Bottom-left
        }

        if (endIndex < availableContracts.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("Next Page")); // This can be made configurable too
            next.setItemMeta(nextMeta);
            gui.setItem(53, next); // Bottom-right
        }

        player.openInventory(gui);
    }

    private ItemStack createContractItem(Contract contract) {
        ItemStack item = new ItemStack(contract.itemType());
        ItemMeta meta = item.getItemMeta();

        // Title
        meta.displayName(plugin.getConfigManager().getMessage("gui-contract-title", player, false,
                Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                Placeholder.unparsed("item", contract.itemType().name())
        ));

        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(plugin.getConfigManager().getMessage("gui-lore-creator", player, false,
                Placeholder.unparsed("player", contract.creatorName())));

        lore.add(plugin.getConfigManager().getMessage("gui-lore-reward", player, false,
                Placeholder.unparsed("reward", String.format("%,.2f", contract.reward()))));

        if (contract.timeLimit() > 0) {
            lore.add(plugin.getConfigManager().getMessage("gui-lore-time", player, false,
                    Placeholder.unparsed("time", TimeParser.formatTime(contract.timeLimit() - System.currentTimeMillis()))));
        }

        lore.add(Component.empty());

        lore.add(plugin.getConfigManager().getMessage("gui-lore-accept", player, false));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
