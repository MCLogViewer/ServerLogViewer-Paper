package fish.crafting.logviewer.packet.s2c.log

import fish.crafting.logviewer.logs.runtime.LogLine
import fish.crafting.logviewer.packet.OutPacket
import org.bukkit.entity.Player

/**
 * Format:
 * STRING - Log ID
 * INT - Line Amount
 *  STRING - Line
 *  BYTE - Log Level
 *  SHORT - Plugin
 */
object S2CLogLinesPacket : OutPacket("log_lines") {
    fun send(player: Player, id: String, lines: List<LogLine>){
        send(player) {
            it.writeUTF(id)
            it.writeInt(lines.size)

            for (line in lines) {
                it.writeUTF(line.line)
                it.writeByte(line.level.ordinal)
                it.writeShort(line.plugin.toInt())
            }
        }
    }
}