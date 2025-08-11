package com.kartaquest.commands;

import com.kartaquest.KartaQuest;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReputationsCommand implements CommandExecutor {

    private final KartaQuest plugin;

    public ReputationsCommand(KartaQuest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-only-command"));
                return true;
            }
            int reputation = plugin.getReputationManager().getReputation(player.getUniqueId());
            player.sendMessage(plugin.getConfigManager().getMessage("reputation-check-self",
                    Placeholder.unparsed("reputation", String.valueOf(reputation))
            ));
            return true;
        }

        if (args.length == 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }
            int reputation = plugin.getReputationManager().getReputation(target.getUniqueId());
            sender.sendMessage(plugin.getConfigManager().getMessage("reputation-check-other",
                    Placeholder.unparsed("player", target.getName()),
                    Placeholder.unparsed("reputation", String.valueOf(reputation))
            ));
            return true;
        }

        sender.sendMessage("Usage: /reputation [player]"); // This can be a configurable message too
        return true;
    }
}
