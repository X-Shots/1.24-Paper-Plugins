package me.cse310.tPP

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CommandTPP(private val plugin: TeleportPlusPlus) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can use this command!")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /TPP <save [name] | [name] | list>")
            return true
        }

        when (args[0].lowercase()) {
            "save" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUsage: /TPP save [name]")
                    return true
                }
                val name = args[1].lowercase()
                val loc = sender.location

                plugin.config.set("saved_locations.$name.world", loc.world?.name)
                plugin.config.set("saved_locations.$name.x", loc.x)
                plugin.config.set("saved_locations.$name.y", loc.y)
                plugin.config.set("saved_locations.$name.z", loc.z)
                plugin.config.set("saved_locations.$name.yaw", loc.yaw)
                plugin.config.set("saved_locations.$name.pitch", loc.pitch)
                plugin.saveConfig()

                sender.sendMessage("§aLocation '$name' saved!")
                return true
            }
            "remove" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUsage: /TPP remove [name]")
                    return true
                }
                val name = args[1].lowercase()
                val locations = plugin.config.getConfigurationSection("saved_locations")?.getKeys(false)

                if (locations == null || !locations.contains(name)) {
                    sender.sendMessage("§cLocation '$name' not found! Use /TPP list to see saved locations.")
                    return true
                }

                // Remove the location from the config
                plugin.config.set("saved_locations.$name", null)
                plugin.saveConfig()

                sender.sendMessage("§aLocation '$name' removed!")
                return true

            }
            "list" -> {
                val locations = plugin.config.getConfigurationSection("saved_locations")?.getKeys(false)
                if (locations.isNullOrEmpty()) {
                    sender.sendMessage("§cNo saved locations found!")
                } else {
                    sender.sendMessage("§aSaved locations:")
                    locations.forEach { name ->
                        val x = plugin.config.getDouble("saved_locations.$name.x")
                        val y = plugin.config.getDouble("saved_locations.$name.y")
                        val z = plugin.config.getDouble("saved_locations.$name.z")
                        sender.sendMessage("§e- $name: ($x, $y, $z)")
                    }
                }
                return true
            }
            else -> {
                val name = args[0].lowercase()
                val worldName = plugin.config.getString("saved_locations.$name.world")
                if (worldName == null) {
                    sender.sendMessage("§cLocation '$name' not found! Use /TPP list to see saved locations.")
                    return true
                }

                val world = Bukkit.getWorld(worldName)
                if (world == null) {
                    sender.sendMessage("§cWorld '$worldName' not found!")
                    return true
                }

                val x = plugin.config.getDouble("saved_locations.$name.x")
                val y = plugin.config.getDouble("saved_locations.$name.y")
                val z = plugin.config.getDouble("saved_locations.$name.z")
                val yaw = plugin.config.getDouble("saved_locations.$name.yaw").toFloat()
                val pitch = plugin.config.getDouble("saved_locations.$name.pitch").toFloat()

                val loc = Location(world, x, y, z, yaw, pitch)
                sender.teleport(loc)
                sender.sendMessage("§aTeleported to '$name'!")
                return true
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (sender !is Player) return null

        // Get all saved location names
        val locations = plugin.config.getConfigurationSection("saved_locations")?.getKeys(false) ?: return null

        return when (args.size) {
            1 -> {

                val suggestions = mutableListOf("");
                suggestions.addAll(locations)
                suggestions.filter { it.startsWith(args[0].lowercase()) }
            }
            2 -> {
                // Suggest nothing for /TPP save [name] (let the player type a new name)
                if (args[0].lowercase() == "save") emptyList() else null
            }
            else -> null // No suggestions for additional arguments
        }
    }
}