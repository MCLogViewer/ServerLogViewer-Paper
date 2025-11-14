package fish.crafting.logviewer.packet.s2c.log

import fish.crafting.logviewer.packet.OutPacket
import org.bukkit.entity.Player

/**
 * Format:
 * INT - stitch index
 * STRING - ID of the Log
 *
 * INT - Data Length
 * BYTES - Data
 */
object S2CLogContentsPacket : OutPacket("log_contents") {
    fun send(player: Player, data: ByteArray, index: Int, id: String){
        send(player) {
            it.writeInt(index)
            it.writeUTF(id)

            it.writeInt(data.size)
            it.write(data)
        }
    }
}