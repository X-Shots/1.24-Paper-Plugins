package me.cse310.tPP

import org.bukkit.plugin.java.JavaPlugin

class TeleportPlusPlus : JavaPlugin() {
    override fun onEnable() {
        logger.info("TeleportPlusPlus has been enabled!")
        saveDefaultConfig() // Ensures config.yml exists

        // Register the TPP command with executor and tab completer
        val commandTPP = CommandTPP(this)
        getCommand("TPP")?.setExecutor(commandTPP)
        getCommand("TPP")?.tabCompleter = commandTPP
    }

    override fun onDisable() {
        logger.info("Thank you using TPP")
    }
}