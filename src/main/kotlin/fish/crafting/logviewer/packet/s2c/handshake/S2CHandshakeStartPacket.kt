package fish.crafting.logviewer.packet.s2c.handshake

import fish.crafting.logviewer.ServerLogViewer
import fish.crafting.logviewer.packet.OutPacket
import fish.crafting.logviewer.util.runLater
import org.bukkit.entity.Player

/**
 * INT - Compatibility version
 */
object S2CHandshakeStartPacket : OutPacket("handshake_start") {

    fun send(player: Player){
        send(player) {
            it.writeInt(ServerLogViewer.COMPATIBILITY_VERSION)
        }
    }

}