package fish.crafting.logviewer.packet.s2c.log

import fish.crafting.logviewer.packet.OutPacket
import org.bukkit.entity.Player

/**
 * Format:
 * STRING - Log ID
 * SHORT - Stitch parts
 */
object S2CInitiateLogPacket : OutPacket("log_data") {
    fun send(player: Player, parts: Int, id: String){
        send(player) {
            it.writeUTF(id)
            it.writeInt(parts)
        }
    }
}