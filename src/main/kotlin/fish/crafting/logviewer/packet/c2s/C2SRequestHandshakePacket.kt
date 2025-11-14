package fish.crafting.logviewer.packet.c2s

import fish.crafting.logviewer.packet.InPacket
import fish.crafting.logviewer.player.PlayerManager
import fish.crafting.logviewer.player.canUseSLV
import fish.crafting.logviewer.util.Cooldowns
import fish.crafting.logviewer.util.failedCooldown
import org.bukkit.entity.Player
import java.io.DataInputStream

object C2SRequestHandshakePacket : InPacket("request_handshake") {
    override fun onReceived(player: Player, stream: DataInputStream) {
        stream.readByte()

        if(!player.failedCooldown(Cooldowns.REQUEST_HANDSHAKE)){
            PlayerManager.handleSLVConnected(player)
        }
    }
}