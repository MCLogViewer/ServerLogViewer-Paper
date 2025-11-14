package fish.crafting.logviewer.player

import fish.crafting.fish.crafting.logviewer.util.send
import fish.crafting.logviewer.ServerLogViewer
import fish.crafting.logviewer.config.ConfigManager
import fish.crafting.logviewer.logs.file.LogFileManager
import fish.crafting.logviewer.packet.s2c.handshake.S2CHandshakeStartPacket
import fish.crafting.logviewer.packet.s2c.log.S2CSetupStatePacket
import fish.crafting.logviewer.packet.s2c.log.S2CStopConnectionPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object PlayerManager {

    private val activePlayers = hashSetOf<UUID>()

    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ServerLogViewer.plugin, {
            PlayerMemory.forEach {
                it.handleScheduledLines()
            }
        }, 0, 5);
    }

    fun isUsingLogViewer(player: Player) = player.uniqueId in activePlayers

    fun handleSLVConnected(player: Player) {
        if(!ConfigManager.isSetupCorrectly()){
            S2CSetupStatePacket.send(player)
            return
        }

        if(!player.canUseSLV()) return
        S2CHandshakeStartPacket.send(player)
    }

    fun handleHandshakeComplete(player: Player){
        if(!player.canUseSLV()) {
            handleFakeUser(player)
            return
        }

        activePlayers.add(player.uniqueId)
        LogFileManager.sendLogs(player)
    }

    fun handleQuit(player: Player){
        PlayerMemory.remove(player.uniqueId)
        disconnectFromSLV(player)
    }

    fun disconnectFromSLV(player: Player, sendDisconnectPacket: Boolean = false){
        activePlayers.remove(player.uniqueId)
        if(sendDisconnectPacket){
            S2CStopConnectionPacket.send(player)
        }
    }

    /**
     * Slippery snakes trying to fake handshake packets
     */
    private fun handleFakeUser(player: Player){

    }

    fun forEachSLVUser(forEach: (Player) -> Unit) {
        for (uUID in activePlayers) {
            Bukkit.getPlayer(uUID)?.let(forEach)
        }
    }

    fun handleConfigSetup(wasNotSetupBefore: Boolean) {
        for (player in Bukkit.getOnlinePlayers()) {
            if(player.uniqueId in activePlayers) continue

            //To make the config screen go away for non-users. For users,
            //connecting will make the setup state automatically true.
            if(!player.canUseSLV()){
                if(wasNotSetupBefore){
                    S2CSetupStatePacket.send(player)
                }

                return
            }

            handleSLVConnected(player)
        }
    }

}

fun Player.isUsingLogViewer() = PlayerManager.isUsingLogViewer(this)
fun Player.canUseSLV() = ConfigManager.canUseSLV(this)