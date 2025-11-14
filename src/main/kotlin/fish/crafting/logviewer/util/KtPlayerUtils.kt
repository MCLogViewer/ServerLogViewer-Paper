package fish.crafting.fish.crafting.logviewer.util

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

fun Audience.send(formatted: String) {
    this.sendMessage(MiniMessage.miniMessage().deserialize(formatted))
}