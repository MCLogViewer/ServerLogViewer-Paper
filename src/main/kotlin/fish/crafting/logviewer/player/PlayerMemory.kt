package fish.crafting.logviewer.player

import fish.crafting.logviewer.ServerLogViewer
import fish.crafting.logviewer.logs.runtime.LogLine
import fish.crafting.logviewer.packet.s2c.log.S2CLogLinesPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class PlayerMemory(val uuid: UUID) {

    private var finishedStitching = false
    private val lines: ArrayList<LogLine> = arrayListOf()
    private var latestId: String = ""
    private var lastSendTime = 0L

    fun finishStitching(){
        finishedStitching = true
    }

    fun handleLogLine(player: Player, latestLogId: String, logLine: LogLine) {
        if(latestId != latestLogId) {
            lines.clear()
            latestId = latestLogId
        }

        val timeSinceLastSend = System.currentTimeMillis() - lastSendTime;
        lines.add(logLine)

        if(timeSinceLastSend > 500){
            sendPendingLines(player)
        }
    }

    fun handleScheduledLines(){
        if(lines.isEmpty()) return;

        val timeSinceLastSend = System.currentTimeMillis() - lastSendTime;
        if(timeSinceLastSend > 500) {
            val player = Bukkit.getPlayer(uuid) ?: return
            sendPendingLines(player)
        }
    }

    private fun sendPendingLines(player: Player){
        if(!finishedStitching) return

        S2CLogLinesPacket.send(player, latestId, lines)
        lastSendTime = System.currentTimeMillis()
        lines.clear()
    }

    companion object {
        private val map = hashMapOf<UUID, PlayerMemory>()
        fun of(player: Player) = map.computeIfAbsent(player.uniqueId) { PlayerMemory(it) }
        fun forEach(forEach: (PlayerMemory) -> Unit){
            map.values.forEach(forEach)
        }
        fun remove(uuid: UUID) {
            map.remove(uuid)
        }
    }
}

fun Player.getMemory() = PlayerMemory.of(this)