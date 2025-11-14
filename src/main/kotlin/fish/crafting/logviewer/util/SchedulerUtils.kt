package fish.crafting.logviewer.util

import fish.crafting.logviewer.ServerLogViewer
import org.bukkit.Bukkit

fun Any.runLater(ticks: Long, task: () -> Unit) {
    Bukkit.getScheduler().runTaskLater(ServerLogViewer.plugin, task, ticks)
}