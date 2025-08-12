package com.kartaquest.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Contract {

    private final UUID contractId;
    private final UUID creatorUuid;
    private final String creatorName;
    private final Material itemType;
    private final int itemAmount;
    private final double reward;
    private ContractStatus status;
    private UUID assigneeUuid;
    private final long creationTimestamp;
    private final long timeLimit; // 0 for no limit
    private ItemStack completedItem; // The actual item stack delivered by the completer

    public enum ContractStatus {
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED_UNCLAIMED,
        EXPIRED
    }

    public Contract(UUID contractId, UUID creatorUuid, String creatorName, Material itemType, int itemAmount, double reward, ContractStatus status, UUID assigneeUuid, long creationTimestamp, long timeLimit, ItemStack completedItem) {
        this.contractId = contractId;
        this.creatorUuid = creatorUuid;
        this.creatorName = creatorName;
        this.itemType = itemType;
        this.itemAmount = itemAmount;
        this.reward = reward;
        this.status = status;
        this.assigneeUuid = assigneeUuid;
        this.creationTimestamp = creationTimestamp;
        this.timeLimit = timeLimit;
        this.completedItem = completedItem;
    }

    // Getters
    public UUID getContractId() { return contractId; }
    public UUID getCreatorUuid() { return creatorUuid; }
    public String getCreatorName() { return creatorName; }
    public Material getItemType() { return itemType; }
    public int getItemAmount() { return itemAmount; }
    public double getReward() { return reward; }
    public ContractStatus getStatus() { return status; }
    public UUID getAssigneeUuid() { return assigneeUuid; }
    public long getCreationTimestamp() { return creationTimestamp; }
    public long getTimeLimit() { return timeLimit; }
    public ItemStack getCompletedItem() { return completedItem; }

    // Setters for mutable fields
    public void setStatus(ContractStatus status) { this.status = status; }
    public void setAssigneeUuid(UUID assigneeUuid) { this.assigneeUuid = assigneeUuid; }
    public void setCompletedItem(ItemStack completedItem) { this.completedItem = completedItem; }

    public ItemStack getDisplayItem() {
        return new ItemStack(itemType, 1);
    }
}
