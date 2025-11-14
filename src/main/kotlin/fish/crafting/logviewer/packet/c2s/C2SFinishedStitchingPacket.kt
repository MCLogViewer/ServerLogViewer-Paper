package fish.crafting.logviewer.packet.c2s

import fish.crafting.logviewer.logs.runtime.RuntimeLogManager
import fish.crafting.logviewer.packet.InPacket
import fish.crafting.logviewer.player.PlayerManager
import fish.crafting.logviewer.player.getMemory
import fish.crafting.logviewer.player.isUsingLogViewer
import org.bukkit.entity.Player
import java.io.DataInputStream

object C2SFinishedStitchingPacket : InPacket("finished_stitching") {
    override fun onReceived(player: Player, stream: DataInputStream) {
        stream.readByte()

        if(!player.isUsingLogViewer()) return

        RuntimeLogManager.sendPluginsAndIndex(player)
        player.getMemory().finishStitching()
    }
}