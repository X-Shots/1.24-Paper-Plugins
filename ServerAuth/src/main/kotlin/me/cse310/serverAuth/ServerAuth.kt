package me.cse310.serverAuth

import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class ServerAuth : JavaPlugin() {
    private val authenticated = java.util.Collections.synchronizedSet(mutableSetOf<UUID>())
    private lateinit var password: String

    override fun onEnable() {
        saveDefaultConfig()
        password = config.getString("password") ?: run {
            logger.warning("Password not set in config.yml. Using default password.")
            "defaultpassword"
        }
        server.pluginManager.registerEvents(AuthListener(this), this)
        logger.info("ServerAuth enabled!")
    }

    fun isAuthenticated(uuid: UUID): Boolean = authenticated.contains(uuid)

    fun authenticate(uuid: UUID) {
        authenticated.add(uuid)
    }

    fun deauthenticate(uuid: UUID) {
        authenticated.remove(uuid)
    }

    fun getPassword(): String = password

    override fun onDisable() {
        // Plugin shutdown logic
    }
}