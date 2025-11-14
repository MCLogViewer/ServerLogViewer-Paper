package fish.crafting.logviewer.player

import fish.crafting.logviewer.util.runLater
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent

class PlayerListener : Listener {

    @EventHandler
    fun onChannel(event: PlayerRegisterChannelEvent){
        if(event.channel == "slv:s2c_handshake_start"){
            runLater(5) {
                PlayerManager.handleSLVConnected(event.player)
            }
        }
    }

    /*@EventHandler
    fun onJoin(event: PlayerInteractEvent) {
        val item = event.item ?: return
        if(item.type == Material.STONE_SWORD){
            event.player.send("<red>Filling console with errors...")

            for (i in 0 until 500) {
                NullPointerException().printStackTrace()
                ServerLogViewer.logger.severe("This is a severe error!")
                ServerLogViewer.logger.severe("123")
            }
        }else if(item.type == Material.WOODEN_SWORD){
            event.player.send("<red>Filling console with error...")

            NullPointerException().printStackTrace()
        }else if(item.type == Material.DIAMOND_SWORD){
            PlayerManager.handleConnected(event.player)
        }
    }*/

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        PlayerManager.handleQuit(event.player)
    }

}