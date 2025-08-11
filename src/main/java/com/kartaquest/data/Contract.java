package com.kartaquest.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record Contract(
    UUID contractId,
    UUID creatorUuid,
    String creatorName,
    Material itemType,
    int itemAmount,
    double reward,
    ContractStatus status,
    UUID assigneeUuid,
    long creationTimestamp,
    long timeLimit // 0 for no limit
) {

    public enum ContractStatus {
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED_UNCLAIMED,
        EXPIRED
    }

    public ItemStack getDisplayItem() {
        return new ItemStack(itemType, 1);
    }
}
