package fish.crafting.logviewer.packet.s2c.log

import fish.crafting.logviewer.logs.runtime.LogIndex
import fish.crafting.logviewer.packet.OutPacket
import org.bukkit.entity.Player

/**
 * Sent at the beginning to initialize plugin data for file-read lines
 *
 * Format:
 * INT - indexBegin
 * INT - Line Indices Amount
 *  SHORT - Plugin ID
 */
object S2CPluginIndexPacket : OutPacket("plugin_index") {
    fun send(player: Player, index: LogIndex){
        send(player) {
            it.writeInt(index.getIndexBegin())

            val indices = index.getIndices()
            it.writeInt(indices.size)
            indices.forEach { s ->
                it.writeShort(s.toInt())
            }
        }
    }
}