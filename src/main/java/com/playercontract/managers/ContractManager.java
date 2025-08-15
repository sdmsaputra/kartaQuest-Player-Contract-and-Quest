package com.playercontract.managers;

import com.playercontract.PlayerContract;
import com.playercontract.data.Contract;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ContractManager {

    private final PlayerContract plugin;
    private final Map<UUID, Contract> activeContracts = new ConcurrentHashMap<>();

    public ContractManager(PlayerContract plugin) {
        this.plugin = plugin;
        loadContracts();
    }

    public void loadContracts() {
        new BukkitRunnable() {
            @Override
            public void run() {
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
                    long acceptedTimestamp = plugin.getDataManager().getContractsConfig().getLong(path + ".acceptedTimestamp", 0);
                    long completedTimestamp = plugin.getDataManager().getContractsConfig().getLong(path + ".completedTimestamp", 0);
                    long timeLimit = plugin.getDataManager().getContractsConfig().getLong(path + ".timeLimit");

                    Contract contract = new Contract(contractId, creatorUuid, creatorName, itemType, itemAmount, reward, status, assigneeUuid, creationTimestamp, acceptedTimestamp, completedTimestamp, timeLimit);
                    activeContracts.put(contractId, contract);
                }
                plugin.getLogger().info("Loaded " + activeContracts.size() + " contracts.");
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveContracts() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getDataManager().getContractsConfig().set("contracts", null); // Clear old data
                for (Map.Entry<UUID, Contract> entry : activeContracts.entrySet()) {
                    String path = "contracts." + entry.getKey().toString();
                    Contract contract = entry.getValue();
                    plugin.getDataManager().getContractsConfig().set(path + ".creatorUuid", contract.creatorUuid().toString());
                    plugin.getDataManager().getContractsConfig().set(path + ".creatorName", contract.creatorName());
                    plugin.getDataManager().getContractsConfig().set(path + ".itemType", contract.itemType().name());
                    plugin.getDataManager().getContractsConfig().set(path + ".itemAmount", contract.itemAmount());
                    plugin.getDataManager().getContractsConfig().set(path + ".reward", contract.reward());
                    plugin.getDataManager().getContractsConfig().set(path + ".status", contract.status().name());
                    plugin.getDataManager().getContractsConfig().set(path + ".assigneeUuid", contract.assigneeUuid() != null ? contract.assigneeUuid().toString() : null);
                    plugin.getDataManager().getContractsConfig().set(path + ".creationTimestamp", contract.creationTimestamp());
                    plugin.getDataManager().getContractsConfig().set(path + ".acceptedTimestamp", contract.acceptedTimestamp());
                    plugin.getDataManager().getContractsConfig().set(path + ".completedTimestamp", contract.completedTimestamp());
                    plugin.getDataManager().getContractsConfig().set(path + ".timeLimit", contract.timeLimit());
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
            plugin.getDataManager().getContractsConfig().set(path + ".creatorUuid", contract.creatorUuid().toString());
            plugin.getDataManager().getContractsConfig().set(path + ".creatorName", contract.creatorName());
            plugin.getDataManager().getContractsConfig().set(path + ".itemType", contract.itemType().name());
            plugin.getDataManager().getContractsConfig().set(path + ".itemAmount", contract.itemAmount());
            plugin.getDataManager().getContractsConfig().set(path + ".reward", contract.reward());
            plugin.getDataManager().getContractsConfig().set(path + ".status", contract.status().name());
            plugin.getDataManager().getContractsConfig().set(path + ".assigneeUuid", contract.assigneeUuid() != null ? contract.assigneeUuid().toString() : null);
            plugin.getDataManager().getContractsConfig().set(path + ".creationTimestamp", contract.creationTimestamp());
            plugin.getDataManager().getContractsConfig().set(path + ".acceptedTimestamp", contract.acceptedTimestamp());
            plugin.getDataManager().getContractsConfig().set(path + ".completedTimestamp", contract.completedTimestamp());
            plugin.getDataManager().getContractsConfig().set(path + ".timeLimit", contract.timeLimit());
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
                0,
                0,
                timeLimit
        );
        activeContracts.put(contractId, contract);
    }

    public Map<UUID, Contract> getActiveContracts() {
        return activeContracts;
    }

    public List<Contract> getAvailableContracts() {
        return activeContracts.values().stream()
                .filter(c -> c.status() == Contract.ContractStatus.AVAILABLE)
                .toList();
    }

    public Contract getContract(UUID contractId) {
        return activeContracts.get(contractId);
    }

    public boolean hasActiveContract(UUID playerUuid) {
        return activeContracts.values().stream()
                .anyMatch(c -> c.assigneeUuid() != null && c.assigneeUuid().equals(playerUuid));
    }

    public void acceptContract(UUID contractId, UUID playerUuid) {
        Contract oldContract = activeContracts.get(contractId);
        if (oldContract == null || oldContract.status() != Contract.ContractStatus.AVAILABLE) {
            return; // Or throw an exception
        }
        Contract newContract = new Contract(
                oldContract.contractId(),
                oldContract.creatorUuid(),
                oldContract.creatorName(),
                oldContract.itemType(),
                oldContract.itemAmount(),
                oldContract.reward(),
                Contract.ContractStatus.IN_PROGRESS,
                playerUuid,
                oldContract.creationTimestamp(),
                System.currentTimeMillis(),
                0,
                oldContract.timeLimit()
        );
        activeContracts.put(contractId, newContract);
    }

    public Contract getContractByAssignee(UUID playerUuid) {
        return activeContracts.values().stream()
                .filter(c -> c.status() == Contract.ContractStatus.IN_PROGRESS && playerUuid.equals(c.assigneeUuid()))
                .findFirst()
                .orElse(null);
    }

    public void completeContract(UUID contractId) {
        Contract oldContract = activeContracts.get(contractId);
        if (oldContract == null) return;

        Contract newContract = new Contract(
                oldContract.contractId(),
                oldContract.creatorUuid(),
                oldContract.creatorName(),
                oldContract.itemType(),
                oldContract.itemAmount(),
                oldContract.reward(),
                Contract.ContractStatus.COMPLETED_UNCLAIMED,
                oldContract.assigneeUuid(),
                oldContract.creationTimestamp(),
                oldContract.acceptedTimestamp(),
                System.currentTimeMillis(),
                oldContract.timeLimit()
        );
        activeContracts.put(contractId, newContract);
    }

    public void cancelContract(UUID contractId) {
        Contract oldContract = activeContracts.get(contractId);
        if (oldContract == null) return;

        Contract newContract = new Contract(
                oldContract.contractId(),
                oldContract.creatorUuid(),
                oldContract.creatorName(),
                oldContract.itemType(),
                oldContract.itemAmount(),
                oldContract.reward(),
                Contract.ContractStatus.AVAILABLE,
                null, // Remove assignee
                oldContract.creationTimestamp(),
                0,
                0,
                oldContract.timeLimit()
        );
        activeContracts.put(contractId, newContract);
    }

    public List<Contract> getClaimableContracts(UUID creatorUuid) {
        return activeContracts.values().stream()
                .filter(c -> c.status() == Contract.ContractStatus.COMPLETED_UNCLAIMED && creatorUuid.equals(c.creatorUuid()))
                .toList();
    }

    public void removeContract(UUID contractId) {
        activeContracts.remove(contractId);
    }

    public void expireContract(UUID contractId) {
        Contract oldContract = activeContracts.get(contractId);
        if (oldContract == null) return;

        Contract newContract = new Contract(
                oldContract.contractId(),
                oldContract.creatorUuid(),
                oldContract.creatorName(),
                oldContract.itemType(),
                oldContract.itemAmount(),
                oldContract.reward(),
                Contract.ContractStatus.EXPIRED,
                oldContract.assigneeUuid(),
                oldContract.creationTimestamp(),
                oldContract.acceptedTimestamp(),
                oldContract.completedTimestamp(),
                oldContract.timeLimit()
        );
        activeContracts.put(contractId, newContract);
    }

    public List<Contract> getInProgressContracts(UUID playerUuid) {
        return activeContracts.values().stream()
                .filter(c -> c.status() == Contract.ContractStatus.IN_PROGRESS && playerUuid.equals(c.assigneeUuid()))
                .toList();
    }

    public List<Contract> getCompletedContracts(UUID playerUuid) {
        return activeContracts.values().stream()
                .filter(c -> c.status() == Contract.ContractStatus.COMPLETED_UNCLAIMED && playerUuid.equals(c.assigneeUuid()))
                .toList();
    }

    public List<Contract> getContractsByCreator(UUID creatorUuid) {
        return activeContracts.values().stream()
                .filter(c -> c.creatorUuid().equals(creatorUuid))
                .toList();
    }
}
