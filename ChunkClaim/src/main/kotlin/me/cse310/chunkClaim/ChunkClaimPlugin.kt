package me.cse310.chunkClaim

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

@Serializable
data class ChunkCoords(val x: Int, val z: Int)

@Serializable
data class ClaimData(
    val claimedChunks: Map<ChunkCoords, String>, // Stores UUID as String
    val playerClaimCount: Map<String, Int>      // Stores claim count per UUID
)

class ChunkClaimPlugin : JavaPlugin(), Listener {
    private val claimedChunks = mutableMapOf<ChunkCoords, UUID>()
    private val playerClaimCount = mutableMapOf<UUID, Int>()
    private val dataFile = File(dataFolder, "claims.json")
    private val json = Json { prettyPrint = true }

    override fun onEnable() {
        logger.info("ChunkClaimPlugin enabled!")
        server.pluginManager.registerEvents(this, this)
        getCommand("claimchunk")?.setExecutor { sender, _, _, _ ->
            if (sender !is Player) {
                sender.sendMessage("Only players can use this command!")
                return@setExecutor true
            }
            claimChunk(sender)
            true
        }
        getCommand("unclaimchunk")?.setExecutor { sender, _, _, _ ->
            if (sender !is Player) {
                sender.sendMessage("Only players can use this command!")
                return@setExecutor true
            }
            unclaimChunk(sender)
            true
        }
        loadClaims()
    }

    private fun loadClaims() {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        if (!dataFile.exists()) return

        try {
            val data = json.decodeFromString<ClaimData>(dataFile.readText())
            claimedChunks.clear()
            playerClaimCount.clear()
            data.claimedChunks.forEach { (coords, uuid) -> claimedChunks[coords] = UUID.fromString(uuid) }
            data.playerClaimCount.forEach { (uuid, count) -> playerClaimCount[UUID.fromString(uuid)] = count }
            logger.info("Loaded ${claimedChunks.size} claimed chunks.")
        } catch (e: Exception) {
            logger.severe("Error loading claims: ${e.message}")
        }
    }

    override fun onDisable() {
        saveClaims()
        logger.info("ChunkClaimPlugin disabled!")
    }

    private fun saveClaims() {
        try {
            val data = ClaimData(
                claimedChunks = claimedChunks.mapValues { it.value.toString() },
                playerClaimCount = playerClaimCount.mapKeys { it.key.toString() }
            )
            dataFile.writeText(json.encodeToString(data))
            logger.info("Saved ${claimedChunks.size} claimed chunks.")
        } catch (e: Exception) {
            logger.severe("Error saving claims: ${e.message}")
        }
    }

    private fun claimChunk(player: Player) {
        val chunk = player.location.chunk
        val chunkKey = ChunkCoords(chunk.x, chunk.z)
        val currentClaims = playerClaimCount.getOrDefault(player.uniqueId, 0)

        if (currentClaims >= 4) {
            player.sendMessage("You've already claimed the maximum of 4 chunks!")
            return
        }
        if (claimedChunks.containsKey(chunkKey)) {
            player.sendMessage("This chunk is already claimed!")
            return
        }

        claimedChunks[chunkKey] = player.uniqueId
        playerClaimCount[player.uniqueId] = currentClaims + 1
        saveClaims()
        player.sendMessage("Chunk claimed successfully! (${currentClaims + 1}/4)")
    }

    private fun unclaimChunk(player: Player) {
        val chunk = player.location.chunk
        val chunkKey = ChunkCoords(chunk.x, chunk.z)

        if (claimedChunks[chunkKey] == player.uniqueId) {
            claimedChunks.remove(chunkKey)
            playerClaimCount[player.uniqueId] = (playerClaimCount[player.uniqueId] ?: 1) - 1
            saveClaims()
            player.sendMessage("Chunk unclaimed!")
        } else {
            player.sendMessage("You don’t own this chunk!")
        }
    }

    private fun isChunkOwner(player: Player, chunk: Chunk): Boolean {
        return claimedChunks[ChunkCoords(chunk.x, chunk.z)] == player.uniqueId
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val chunkKey = ChunkCoords(event.block.chunk.x, event.block.chunk.z)
        val owner = claimedChunks[chunkKey]
        if (owner != null && owner != event.player.uniqueId) {
            event.isCancelled = true
            event.player.sendMessage("You can't break blocks in someone else's chunk!")
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val chunkKey = ChunkCoords(event.block.chunk.x, event.block.chunk.z)
        val owner = claimedChunks[chunkKey]
        if (owner != null && owner != event.player.uniqueId) {
            event.isCancelled = true
            event.player.sendMessage("You can't place blocks in someone else's chunk!")
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        event.clickedBlock?.let {
            val chunkKey = ChunkCoords(it.chunk.x, it.chunk.z)
            val owner = claimedChunks[chunkKey]
            if (owner != null && owner != event.player.uniqueId) {
                event.isCancelled = true
                event.player.sendMessage("You can't interact with blocks in someone else's chunk!")
            }
        }
    }
}