package fish.crafting.logviewer

import fish.crafting.logviewer.logs.runtime.RuntimeLogManager
import fish.crafting.fish.crafting.logviewer.util.send
import fish.crafting.logviewer.command.SLVCommand
import fish.crafting.logviewer.config.ConfigManager
import fish.crafting.logviewer.logs.file.LogFileManager
import fish.crafting.logviewer.packet.PacketManager
import fish.crafting.logviewer.player.PlayerListener
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.PriorityQueue
import java.util.UUID

class ServerLogViewer : JavaPlugin() {

    companion object {
        lateinit var plugin: ServerLogViewer
        val logger by lazy { plugin.logger }
        val latestLogId by lazy { UUID.randomUUID().toString() }
        const val COMPATIBILITY_VERSION = 1

    }

    override fun onEnable() {
        saveDefaultConfig()

        plugin = this

        RuntimeLogManager.onPluginInitialized()
        val a = ConfigManager
        val b = PacketManager

        registerEvents(
            PlayerListener()
        )

        registerCommand("slv", SLVCommand)
    }

    override fun onDisable() {
        RuntimeLogManager.disable()
        PacketManager.disable()
        LogFileManager.shutdown()
    }

    private fun registerEvents(vararg listeners: Listener){
        val pluginManager = Bukkit.getPluginManager()
        for (listener in listeners) {
            pluginManager.registerEvents(listener, this)
        }
    }

}