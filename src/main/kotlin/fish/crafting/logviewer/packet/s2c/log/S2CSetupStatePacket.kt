package fish.crafting.logviewer.packet.s2c.log

import fish.crafting.logviewer.config.ConfigManager
import fish.crafting.logviewer.packet.OutPacket
import org.bukkit.entity.Player

object S2CSetupStatePacket : OutPacket("slv_setup_state") {
    fun send(player: Player){
        send(player) {
            it.writeBoolean(ConfigManager.isSetupCorrectly())
        }
    }
}