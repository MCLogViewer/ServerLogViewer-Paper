package fish.crafting.logviewer.packet.s2c.log

import fish.crafting.logviewer.packet.OutPacket
import org.bukkit.entity.Player

/**
 * Format:
 * INT - Plugin Amount
 *  SHORT - Plugin ID
 *  STRING - Plugin Name
 */
object S2CRegisterPluginsPacket : OutPacket("register_plugins") {
    fun send(player: Player, plugins: Map<String, Short>){
        if(plugins.isEmpty()) return

        send(player) {
            it.writeInt(plugins.size)
            for (entry in plugins) {
                it.writeShort(entry.value.toInt())
                it.writeUTF(entry.key)
            }
        }
    }

    fun send(player: Player, name: String, id: Short){
        send(player) {
            it.writeInt(1)
            it.writeShort(id.toInt())
            it.writeUTF(name)
        }
    }
}