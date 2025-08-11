package com.kartaquest.gui;

import com.kartaquest.data.Contract;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContractGUIHolder implements InventoryHolder {
    private final Map<Integer, UUID> slotToContractIdMap = new HashMap<>();
    private int page;

    public ContractGUIHolder(int page) {
        this.page = page;
    }

    public void setContractAtSlot(int slot, UUID contractId) {
        slotToContractIdMap.put(slot, contractId);
    }

    public UUID getContractIdAtSlot(int slot) {
        return slotToContractIdMap.get(slot);
    }

    public int getPage() {
        return page;
    }

    @Override
    public @NotNull Inventory getInventory() {
        // This is required by the interface, but we will create the inventory in the GUI class itself.
        // We can leave this null as we will be passing the holder instance directly to Bukkit.createInventory.
        return null;
    }
}
