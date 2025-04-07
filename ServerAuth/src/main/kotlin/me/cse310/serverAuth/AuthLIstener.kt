package me.cse310.serverAuth

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class AuthListener(private val plugin: ServerAuth) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (!plugin.isAuthenticated(player.uniqueId)) {
            player.sendMessage("Please use /login <password> to join the server.")
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.deauthenticate(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (!plugin.isAuthenticated(player.uniqueId)) {
            val args = event.message.split(" ")
            if (args[0].equals("/login", ignoreCase = true)) {
                event.isCancelled = true
                if (args.size == 2 && args[1] == plugin.getPassword()) {
                    plugin.authenticate(player.uniqueId)
                    player.sendMessage("Password accepted. Welcome!")
                } else {
                    player.sendMessage("Incorrect password. Try again.")
                }
            } else {
                event.isCancelled = true
                player.sendMessage("You must login first using /login <password>")
            }
        }
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        if (!plugin.isAuthenticated(event.player.uniqueId)) {
            event.isCancelled = true
            event.player.sendMessage("You must login first using /login <password>")
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!plugin.isAuthenticated(event.player.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!plugin.isAuthenticated(event.player.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!plugin.isAuthenticated(event.player.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!plugin.isAuthenticated(event.player.uniqueId)) {
            event.isCancelled = true
        }
    }
}