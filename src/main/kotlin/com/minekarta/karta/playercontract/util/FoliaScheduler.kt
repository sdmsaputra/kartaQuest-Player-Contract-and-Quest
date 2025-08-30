package com.minekarta.karta.playercontract.util

import com.minekarta.karta.playercontract.KartaPlayerContract
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * A utility class to handle Folia's region-based scheduler vs Bukkit's global scheduler.
 * This ensures that tasks are run on the correct thread.
 */
class FoliaScheduler(private val plugin: KartaPlayerContract) {

    private val isFolia = try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    /**
     * Runs a task on the appropriate main thread for the entity.
     * On Folia, this is the entity's region thread.
     * On Bukkit/Spigot/Paper, this is the global server thread.
     * @return A CompletableFuture that completes when the task is finished.
     */
    fun runOnMainThread(player: Player, task: Runnable): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val fullTask = Runnable {
            try {
                task.run()
                future.complete(null)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        if (isFolia) {
            // For Folia, we need to execute the task on the player's region thread.
            player.scheduler.run(plugin, { _ -> fullTask.run() }, null)
        } else {
            // For traditional Bukkit/Spigot/Paper, run on the main server thread.
            @Suppress("DEPRECATION") // The non-deprecated alternative is Folia-specific
            plugin.server.scheduler.runTask(plugin, fullTask)
        }
        return future
    }

    /**
     * Runs a task asynchronously off the main server thread.
     */
    fun runAsync(task: Runnable): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        plugin.server.asyncScheduler.runNow(plugin) { _ ->
            try {
                task.run()
                future.complete(null)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    /**
     * Runs a supplier asynchronously and returns a CompletableFuture with the result.
     */
    fun <T> supplyAsync(supplier: Supplier<T>): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        plugin.server.asyncScheduler.runNow(plugin) { _ ->
            try {
                future.complete(supplier.get())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }
}
