package net.savagelabs.skyblockx.core

import io.papermc.lib.PaperLib
import net.prosavage.baseplugin.WorldBorderUtil
import net.savagelabs.skyblockx.Globals
import net.savagelabs.skyblockx.persist.Config
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

fun color(message: String): String {
    return ChatColor.translateAlternateColorCodes('&', message)
}

fun buildBar(element: String, barLength: Int = Config.barLength): String {
    val bar = StringBuilder()
    repeat(barLength) {
        bar.append(element)
    }
    return color(bar.toString())
}

fun broadcastDebug(message: String) {
    Bukkit.broadcastMessage(color("Debug -> $message"))
}

fun isNotInSkyblockWorld(world: World): Boolean {
    return world.name != Config.skyblockWorldName && world.name != Config.skyblockWorldNameNether
}

fun teleportAsync(player: Player?, location: Location, runnable: Runnable) {
    if (player == null) return
    PaperLib.teleportAsync(player, location.add(0.0, 1.0, 0.0), PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept {
        if (it) runnable.run() else Globals.skyblockX.logger.severe("Something went horribly wrong when trying to teleport ${player.name}.")
    }
}


inline fun <reified T : Enum<*>> enumValueOrNull(name: String): T? = T::class.java.enumConstants.firstOrNull { it.name == name }

fun updateWorldBorder(player: Player, location: Location, delay: Long) {
    if (isNotInSkyblockWorld(location.world!!)) {
        val worldBorder = location.world?.worldBorder
        Globals.worldBorderUtil.sendWorldBorder(
            player,
            WorldBorderUtil.Color.NONE,
            worldBorder!!.size,
            worldBorder.center
        )
    } else {
        Bukkit.getScheduler()
            .runTaskLater(
                Globals.skyblockX,
                Runnable {
                    val islandFromLocation = getIslandFromLocation(location)
                    val iPlayer = getIPlayer(player)
                    if (islandFromLocation != null) {
                        val wbCenter = islandFromLocation.getIslandCenter()
                        wbCenter.world = player.world
                        WorldBorderUtil(Globals.skyblockX).sendWorldBorder(
                            player,
                            iPlayer.borderColor,
                            islandFromLocation.islandSize.toDouble(),
                            wbCenter
                        )
                    }
                },
                delay
            )
    }
}

fun isPlaceholderAPIPresent(): Boolean {
    return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
}