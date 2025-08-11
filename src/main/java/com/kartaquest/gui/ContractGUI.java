package com.kartaquest.gui;

import com.kartaquest.KartaQuest;
import com.kartaquest.data.Contract;
import com.kartaquest.utils.TimeParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    private final KartaQuest plugin;
    private final Player player;
    private int page;
    private static final int CONTRACTS_PER_PAGE = 45; // 9x5

    public ContractGUI(KartaQuest plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
    }

    public void open() {
        List<Contract> availableContracts = plugin.getContractManager().getAvailableContracts();
        ContractGUIHolder holder = new ContractGUIHolder(page);

        int totalPages = Math.max(1, (int) Math.ceil((double) availableContracts.size() / CONTRACTS_PER_PAGE));
        // We don't add the prefix to the GUI title, so call deserialize directly.
        Component title = MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.gui-title"),
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
            prevMeta.displayName(MiniMessage.miniMessage().deserialize("<gray>Previous Page"));
            previous.setItemMeta(prevMeta);
            gui.setItem(45, previous); // Bottom-left
        }

        if (endIndex < availableContracts.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(MiniMessage.miniMessage().deserialize("<gray>Next Page"));
            next.setItemMeta(nextMeta);
            gui.setItem(53, next); // Bottom-right
        }

        player.openInventory(gui);
    }

    private ItemStack createContractItem(Contract contract) {
        ItemStack item = new ItemStack(contract.itemType());
        ItemMeta meta = item.getItemMeta();
        MiniMessage mm = MiniMessage.miniMessage();

        // Title
        String titleFormat = plugin.getConfig().getString("messages.gui-contract-title", "<gold>Tugas: Kumpulkan {amount} {item}");
        meta.displayName(mm.deserialize(titleFormat,
                Placeholder.unparsed("amount", String.valueOf(contract.itemAmount())),
                Placeholder.unparsed("item", contract.itemType().name())
        ));

        // Lore
        List<Component> lore = new ArrayList<>();
        String creatorFormat = plugin.getConfig().getString("messages.gui-lore-creator", "<gray>Dibuat oleh: <white>{player}");
        lore.add(mm.deserialize(creatorFormat, Placeholder.unparsed("player", contract.creatorName())));

        String rewardFormat = plugin.getConfig().getString("messages.gui-lore-reward", "<gray>Imbalan: <green>${reward}");
        lore.add(mm.deserialize(rewardFormat, Placeholder.unparsed("reward", String.format("%,.2f", contract.reward()))));

        String timeFormat = plugin.getConfig().getString("messages.gui-lore-time", "<gray>Sisa Waktu: <red>{time}");
        lore.add(mm.deserialize(timeFormat, Placeholder.unparsed("time", TimeParser.formatTime(contract.timeLimit()))));

        lore.add(Component.empty());

        String acceptFormat = plugin.getConfig().getString("messages.gui-lore-accept", "<yellow>Klik untuk menerima kontrak!");
        lore.add(mm.deserialize(acceptFormat));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
