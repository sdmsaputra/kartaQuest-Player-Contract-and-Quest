package com.kartaquest.managers;

import com.kartaquest.KartaQuest;
import com.kartaquest.data.Contract;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ContractManager {

    private final KartaQuest plugin;
    private final Map<UUID, Contract> activeContracts = new ConcurrentHashMap<>();

    public ContractManager(KartaQuest plugin) {
        this.plugin = plugin;
        loadContracts();
    }

    public void loadContracts() {
        plugin.getDataManager().reloadContractsConfig();
        ConfigurationSection contractsSection = plugin.getDataManager().getContractsConfig().getConfigurationSection("contracts");
        if (contractsSection == null) return;

        for (String key : contractsSection.getKeys(false)) {
            UUID contractId = UUID.fromString(key);
            String path = "contracts." + key;

            UUID creatorUuid = UUID.fromString(plugin.getDataManager().getContractsConfig().getString(path + ".creatorUuid"));
            String creatorName = plugin.getDataManager().getContractsConfig().getString(path + ".creatorName");
            Material itemType = Material.valueOf(plugin.getDataManager().getContractsConfig().getString(path + ".itemType"));
            int itemAmount = plugin.getDataManager().getContractsConfig().getInt(path + ".itemAmount");
            double reward = plugin.getDataManager().getContractsConfig().getDouble(path + ".reward");
            Contract.ContractStatus status = Contract.ContractStatus.valueOf(plugin.getDataManager().getContractsConfig().getString(path + ".status"));
            String assigneeUuidString = plugin.getDataManager().getContractsConfig().getString(path + ".assigneeUuid");
            UUID assigneeUuid = assigneeUuidString == null || assigneeUuidString.equals("null") ? null : UUID.fromString(assigneeUuidString);
            long creationTimestamp = plugin.getDataManager().getContractsConfig().getLong(path + ".creationTimestamp");
            long timeLimit = plugin.getDataManager().getContractsConfig().getLong(path + ".timeLimit");
            ItemStack completedItem = plugin.getDataManager().getContractsConfig().getItemStack(path + ".completedItem");

            Contract contract = new Contract(contractId, creatorUuid, creatorName, itemType, itemAmount, reward, status, assigneeUuid, creationTimestamp, timeLimit, completedItem);
            activeContracts.put(contractId, contract);
        }
        plugin.getLogger().info("Loaded " + activeContracts.size() + " contracts.");
    }

    public void saveContracts() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getDataManager().getContractsConfig().set("contracts", null); // Clear old data
                for (Map.Entry<UUID, Contract> entry : activeContracts.entrySet()) {
                    String path = "contracts." + entry.getKey().toString();
                    Contract contract = entry.getValue();
                    plugin.getDataManager().getContractsConfig().set(path + ".creatorUuid", contract.getCreatorUuid().toString());
                    plugin.getDataManager().getContractsConfig().set(path + ".creatorName", contract.getCreatorName());
                    plugin.getDataManager().getContractsConfig().set(path + ".itemType", contract.getItemType().name());
                    plugin.getDataManager().getContractsConfig().set(path + ".itemAmount", contract.getItemAmount());
                    plugin.getDataManager().getContractsConfig().set(path + ".reward", contract.getReward());
                    plugin.getDataManager().getContractsConfig().set(path + ".status", contract.getStatus().name());
                    plugin.getDataManager().getContractsConfig().set(path + ".assigneeUuid", contract.getAssigneeUuid() != null ? contract.getAssigneeUuid().toString() : null);
                    plugin.getDataManager().getContractsConfig().set(path + ".creationTimestamp", contract.getCreationTimestamp());
                    plugin.getDataManager().getContractsConfig().set(path + ".timeLimit", contract.getTimeLimit());
                    plugin.getDataManager().getContractsConfig().set(path + ".completedItem", contract.getCompletedItem());
                }
                plugin.getDataManager().saveContractsConfig();
                plugin.getLogger().info("Saved " + activeContracts.size() + " contracts.");
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveContractsSync() {
        plugin.getDataManager().getContractsConfig().set("contracts", null); // Clear old data
        for (Map.Entry<UUID, Contract> entry : activeContracts.entrySet()) {
            String path = "contracts." + entry.getKey().toString();
            Contract contract = entry.getValue();
            plugin.getDataManager().getContractsConfig().set(path + ".creatorUuid", contract.getCreatorUuid().toString());
            plugin.getDataManager().getContractsConfig().set(path + ".creatorName", contract.getCreatorName());
            plugin.getDataManager().getContractsConfig().set(path + ".itemType", contract.getItemType().name());
            plugin.getDataManager().getContractsConfig().set(path + ".itemAmount", contract.getItemAmount());
            plugin.getDataManager().getContractsConfig().set(path + ".reward", contract.getReward());
            plugin.getDataManager().getContractsConfig().set(path + ".status", contract.getStatus().name());
            plugin.getDataManager().getContractsConfig().set(path + ".assigneeUuid", contract.getAssigneeUuid() != null ? contract.getAssigneeUuid().toString() : null);
            plugin.getDataManager().getContractsConfig().set(path + ".creationTimestamp", contract.getCreationTimestamp());
            plugin.getDataManager().getContractsConfig().set(path + ".timeLimit", contract.getTimeLimit());
            plugin.getDataManager().getContractsConfig().set(path + ".completedItem", contract.getCompletedItem());
        }
        plugin.getDataManager().saveContractsConfig();
        plugin.getLogger().info("Saved " + activeContracts.size() + " contracts.");
    }

    public void createContract(UUID creatorUuid, String creatorName, Material itemType, int itemAmount, double reward, long timeLimit) {
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(
                contractId,
                creatorUuid,
                creatorName,
                itemType,
                itemAmount,
                reward,
                Contract.ContractStatus.AVAILABLE,
                null,
                System.currentTimeMillis(),
                timeLimit,
                null
        );
        activeContracts.put(contractId, contract);
    }

    public Map<UUID, Contract> getActiveContracts() {
        return activeContracts;
    }

    public List<Contract> getAvailableContracts() {
        return activeContracts.values().stream()
                .filter(c -> c.getStatus() == Contract.ContractStatus.AVAILABLE)
                .toList();
    }

    public Contract getContract(UUID contractId) {
        return activeContracts.get(contractId);
    }

    public boolean hasActiveContract(UUID playerUuid) {
        return activeContracts.values().stream()
                .anyMatch(c -> c.getAssigneeUuid() != null && c.getAssigneeUuid().equals(playerUuid));
    }

    public void acceptContract(UUID contractId, UUID playerUuid) {
        Contract contract = activeContracts.get(contractId);
        if (contract == null || contract.getStatus() != Contract.ContractStatus.AVAILABLE) {
            return; // Or throw an exception
        }
        contract.setStatus(Contract.ContractStatus.IN_PROGRESS);
        contract.setAssigneeUuid(playerUuid);
    }

    public Contract getContractByAssignee(UUID playerUuid) {
        return activeContracts.values().stream()
                .filter(c -> c.getStatus() == Contract.ContractStatus.IN_PROGRESS && playerUuid.equals(c.getAssigneeUuid()))
                .findFirst()
                .orElse(null);
    }

    public void completeContract(UUID contractId, ItemStack item) {
        Contract contract = activeContracts.get(contractId);
        if (contract == null) return;

        contract.setStatus(Contract.ContractStatus.COMPLETED_UNCLAIMED);
        contract.setCompletedItem(item);
    }

    public void cancelContract(UUID contractId) {
        Contract contract = activeContracts.get(contractId);
        if (contract == null) return;

        contract.setStatus(Contract.ContractStatus.AVAILABLE);
        contract.setAssigneeUuid(null);
    }

    public List<Contract> getClaimableContracts(UUID creatorUuid) {
        return activeContracts.values().stream()
                .filter(c -> c.getStatus() == Contract.ContractStatus.COMPLETED_UNCLAIMED && creatorUuid.equals(c.getCreatorUuid()))
                .toList();
    }

    public void removeContract(UUID contractId) {
        activeContracts.remove(contractId);
    }

    public void expireContract(UUID contractId) {
        Contract contract = activeContracts.get(contractId);
        if (contract == null) return;

        contract.setStatus(Contract.ContractStatus.EXPIRED);
    }
}
