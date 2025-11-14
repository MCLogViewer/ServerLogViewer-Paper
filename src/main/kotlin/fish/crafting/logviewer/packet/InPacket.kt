package fish.crafting.logviewer.packet

import fish.crafting.fish.crafting.logviewer.util.send
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.DataInputStream

abstract class InPacket(private val channel: String) : PluginMessageListener {

    val compiledChannel by lazy { "slv:c2s_$channel" }

    final override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray
    ) {
        if(message.isEmpty() || channel != compiledChannel) return;

        message.inputStream().use { byteStream ->
            DataInputStream(byteStream).use {
                onReceived(player, it)
            }
        }
    }

    abstract fun onReceived(player: Player, stream: DataInputStream)

}
