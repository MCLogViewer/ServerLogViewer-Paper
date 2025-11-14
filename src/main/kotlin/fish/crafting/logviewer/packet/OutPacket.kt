package fish.crafting.logviewer.packet

import fish.crafting.fish.crafting.logviewer.util.send
import fish.crafting.logviewer.ServerLogViewer
import org.bukkit.entity.Player
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

open class OutPacket(private val channel: String) {

    val compiledChannel by lazy { "slv:s2c_$channel" }

    protected fun send(player: Player, write: (DataOutputStream) -> Unit) {
        val arr: ByteArray

        ByteArrayOutputStream().use { byteStream ->
            DataOutputStream(byteStream).use {
                write.invoke(it)
            }

            arr = byteStream.toByteArray() ?: return
        }

        if(arr.isEmpty()) return

        player.sendPluginMessage(ServerLogViewer.plugin, compiledChannel, arr)
    }
}