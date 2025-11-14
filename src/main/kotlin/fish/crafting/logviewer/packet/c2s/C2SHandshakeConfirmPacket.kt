package fish.crafting.logviewer.packet.c2s

import fish.crafting.logviewer.packet.InPacket
import fish.crafting.logviewer.player.PlayerManager
import org.bukkit.entity.Player
import java.io.DataInputStream

object C2SHandshakeConfirmPacket : InPacket("handshake_confirm") {
    override fun onReceived(player: Player, stream: DataInputStream) {
        stream.readByte()
        PlayerManager.handleHandshakeComplete(player)
    }
}