package fish.crafting.logviewer.packet.s2c.log

import fish.crafting.logviewer.packet.OutPacket
import org.bukkit.entity.Player

object S2CStopConnectionPacket : OutPacket("stop_connection") {
    fun send(player: Player){
        send(player) {
            it.writeByte(1)
        }
    }
}