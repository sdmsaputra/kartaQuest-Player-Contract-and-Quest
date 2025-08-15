package com.playercontract.gui;

import com.playercontract.PlayerContract;
import com.playercontract.data.Contract;
import com.playercontract.utils.TimeParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    public PlayerInventoryGUI(PlayerContract plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        PlayerInventoryGUIHolder holder = new PlayerInventoryGUIHolder();
        // Using a hardcoded title for now, can be moved to config later
        Component title = Component.text("Your Created Contracts");
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
        lore.add(Component.text(""));
        lore.add(Component.text("Item: ").color(NamedTextColor.GRAY)
                .append(Component.text(contract.itemAmount() + "x " + contract.itemType().name()).color(NamedTextColor.WHITE)));
        lore.add(Component.text("Reward: ").color(NamedTextColor.GRAY)
                .append(Component.text(String.format("%,.2f", contract.reward())).color(NamedTextColor.GOLD)));


        switch (contract.status()) {
            case AVAILABLE:
                item = new ItemStack(Material.BOOK);
                meta = item.getItemMeta();
                meta.displayName(Component.text("Status: AVAILABLE").color(NamedTextColor.GREEN));
                lore.add(Component.text("This contract is waiting for someone to accept it.").color(NamedTextColor.GRAY));
                break;

            case IN_PROGRESS:
                item = new ItemStack(Material.WRITABLE_BOOK);
                meta = item.getItemMeta();
                meta.displayName(Component.text("Status: IN PROGRESS").color(NamedTextColor.YELLOW));
                OfflinePlayer assignee = Bukkit.getOfflinePlayer(contract.assigneeUuid());
                lore.add(Component.text("Accepted by: ").color(NamedTextColor.GRAY)
                        .append(Component.text(assignee.getName() != null ? assignee.getName() : "Unknown").color(NamedTextColor.WHITE)));
                lore.add(Component.text("Accepted: ").color(NamedTextColor.GRAY)
                        .append(Component.text(TimeParser.formatTimeElapsed(contract.acceptedTimestamp()) + " ago").color(NamedTextColor.WHITE)));
                break;

            case COMPLETED_UNCLAIMED:
                item = new ItemStack(Material.ENCHANTED_BOOK);
                meta = item.getItemMeta();
                meta.displayName(Component.text("Status: COMPLETED").color(NamedTextColor.AQUA));
                OfflinePlayer completer = Bukkit.getOfflinePlayer(contract.assigneeUuid());
                lore.add(Component.text("Completed by: ").color(NamedTextColor.GRAY)
                        .append(Component.text(completer.getName() != null ? completer.getName() : "Unknown").color(NamedTextColor.WHITE)));
                lore.add(Component.text("Completed: ").color(NamedTextColor.GRAY)
                        .append(Component.text(TimeParser.formatTimeElapsed(contract.completedTimestamp()) + " ago").color(NamedTextColor.WHITE)));
                lore.add(Component.text(""));
                lore.add(Component.text("You can claim the items with /pc claim.").color(NamedTextColor.GOLD));
                break;

            case EXPIRED:
                item = new ItemStack(Material.BARRIER);
                meta = item.getItemMeta();
                meta.displayName(Component.text("Status: EXPIRED").color(NamedTextColor.RED));
                lore.add(Component.text("This contract expired and was not completed.").color(NamedTextColor.GRAY));
                break;

            default:
                item = new ItemStack(Material.STONE);
                meta = item.getItemMeta();
                meta.displayName(Component.text("Status: UNKNOWN").color(NamedTextColor.DARK_GRAY));
                break;
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
