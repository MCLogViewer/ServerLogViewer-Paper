package fish.crafting.logviewer.util

import org.bukkit.entity.Player
import java.util.UUID

enum class Cooldowns(val seconds: Int) {
    REQUEST_HANDSHAKE(8)

    ;

    fun failedCooldown(player: Player): Boolean {
        val playerMap = map.computeIfAbsent(player.uniqueId) { hashMapOf() }

        val lastUse = playerMap[this] ?: 0
        val diff = System.currentTimeMillis() - lastUse
        val onCooldown = diff <= seconds * 1000L
        if(onCooldown) return true

        playerMap[this] = System.currentTimeMillis()
        return false
    }

    companion object{
        private val map: HashMap<UUID, HashMap<Cooldowns, Long>> = hashMapOf()
    }

}

fun Player.failedCooldown(cooldowns: Cooldowns) = cooldowns.failedCooldown(this)