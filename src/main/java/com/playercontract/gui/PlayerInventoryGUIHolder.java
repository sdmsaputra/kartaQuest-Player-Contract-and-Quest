package com.playercontract.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInventoryGUIHolder implements InventoryHolder {

    private final Map<Integer, UUID> contractSlots = new HashMap<>();

    @Override
    public @NotNull Inventory getInventory() {
        return null; // The inventory is created and passed to the player, this holder is just for data
    }

    public void setContractAtSlot(int slot, UUID contractId) {
        contractSlots.put(slot, contractId);
    }

    public UUID getContractIdAtSlot(int slot) {
        return contractSlots.get(slot);
    }
}
