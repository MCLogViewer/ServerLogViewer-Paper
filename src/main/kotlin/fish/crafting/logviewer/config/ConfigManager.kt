package fish.crafting.logviewer.config

import fish.crafting.logviewer.ServerLogViewer
import fish.crafting.logviewer.player.PlayerManager
import net.kyori.adventure.text.Component
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.context.ContextSet
import net.luckperms.api.query.Flag
import net.luckperms.api.query.QueryMode
import net.luckperms.api.query.QueryOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.lang.Exception
import java.util.UUID

object ConfigManager {
    private var blockedPlayers = VerificationMethod.NONE
    private var verificationMethod = VerificationMethod.NONE
    private var hideIPs = true
    private var setup = false

    init {
        load()
    }

    fun load(){
        ServerLogViewer.plugin.reloadConfig()

        val config = getConfig()
        val string = config.getString("baseConfig.verification") ?: ""
        verificationMethod = when(string.lowercase()){
            "whitelist" -> VerificationMethod.whitelist("baseConfig.whitelist")
            "permission" -> VerificationMethod.permission()
            "lpgroups" -> VerificationMethod.luckperms()
            else -> VerificationMethod.NONE
        }

        val setupBefore = setup

        hideIPs = config.getBoolean("baseConfig.hideIPs", true)
        blockedPlayers = VerificationMethod.whitelist("baseConfig.blockedPlayers")
        setup = config.getBoolean("baseConfig.setup", true)

        //Handle disconnecting logic
        val disconnectPlayers = arrayListOf<Player>()
        PlayerManager.forEachSLVUser {
            if(!canUseSLV(it)){
                disconnectPlayers.add(it)
            }
        }

        for (player in disconnectPlayers) {
            PlayerManager.disconnectFromSLV(player, true)
        }

        if(setup){
            PlayerManager.handleConfigSetup(!setupBefore)
        }
    }

    fun shouldBlockIPs() = hideIPs
    fun isSetupCorrectly() = setup

    fun canUseSLV(player: Player): Boolean {
        return !blockedPlayers.matches(player) && verificationMethod.matches(player)
    }

    private fun getConfig(): FileConfiguration {
        return ServerLogViewer.plugin.config
    }

    private abstract class VerificationMethod {

        abstract fun matches(player: Player): Boolean

        companion object{
            val NONE = object : VerificationMethod(){
                override fun matches(player: Player) = false
            }

            fun permission() = object : VerificationMethod() {
                override fun matches(player: Player) = player.hasPermission(Permissions.SLV_USE)
            }

            fun whitelist(path: String): VerificationMethod {
                val stringList = getConfig().getStringList(path)
                val names = hashSetOf<String>()
                val uuids = hashSetOf<UUID>()
                for (string in stringList) {
                    if(string.length in 3..16) {
                        names.add(string.lowercase())
                    }else{
                        try{
                            val uuid = UUID.fromString(string)
                            uuids.add(uuid)
                        }catch (_: Exception){}
                    }
                }

                return object : VerificationMethod(){

                    override fun matches(player: Player): Boolean {
                        return player.uniqueId in uuids || player.name.lowercase() in names
                    }
                }
            }

            fun luckperms(): VerificationMethod {
                val groups = getConfig().getStringList("baseConfig.lpGroups")
                val groupNames = hashSetOf<String>()
                groupNames.addAll(groups)

                return object : VerificationMethod(){
                    override fun matches(player: Player): Boolean {
                        try{
                            val lpApi = LuckPermsProvider.get()
                            val user = lpApi.userManager.getUser(player.uniqueId) ?: return false
                            val inheritedGroups = user.getInheritedGroups(user.queryOptions)

                            for (group in inheritedGroups) {
                                if(group.name in groupNames) {
                                    return true
                                }
                            }

                        }catch (_: Exception){}

                        return false
                    }
                }
            }
        }
    }

}