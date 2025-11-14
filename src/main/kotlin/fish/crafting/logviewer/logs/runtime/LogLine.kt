package fish.crafting.logviewer.logs.runtime

import fish.crafting.logviewer.config.ConfigManager
import fish.crafting.logviewer.logs.LogLevel
import fish.crafting.logviewer.logs.runtime.LogLine.Companion.ipRegex
import java.util.*

data class LogLine(val line: String, val level: LogLevel, val plugin: Short){

    fun compile(): String {
        val levelChar = level.ordinal.toChar()
        val pluginChar = plugin.toInt().toChar()

        val compiled = "$levelChar$line"

        return compiled
    }

    companion object{
        private val timeRegex = Regex("""\[\d{1,2}:[0-5]\d:[0-5]\d]""")
        val ipRegex = Regex("""\b(?:(?:25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(?:25[0-5]|2[0-4]\d|1?\d?\d)(?::\d{1,5})?\b""")

        fun convertPrePluginLine(logLine: String): String{
            val line = logLine.blockIPsIfNeeded()
            if(!line.startsWith("[")) return "${0.toChar()}$line"

            val timeEnd = line.indexOf(']')
            if (timeEnd == -1) return "${0.toChar()}$line"

            if(!line.take(timeEnd + 1).matches(timeRegex)) return "${0.toChar()}$line"

            val threadStart = timeEnd + 2
            if(threadStart >= line.length) return "${0.toChar()}$line"

            if(line[threadStart] != '[') return "${0.toChar()}${line.substring(threadStart)}"
            val slash = line.indexOf('/', threadStart)
            val threadEnd = line.indexOf(']', slash)

            val levelStr = line.substring(slash + 1, threadEnd)
            var level: LogLevel
            try {
                level = LogLevel.valueOf(levelStr.uppercase())
            } catch (_: Exception) {
                return "${0.toChar()}${line.substring(threadStart)}"
            }

            var messageStart = threadEnd + 3
            if(line[messageStart] == '[' && !line.endsWith("]")){ //Has classpath
                val pluginEnd = line.indexOf(']', messageStart)
                if(pluginEnd != -1){
                    messageStart = pluginEnd + 2
                }
            }

            if(messageStart >= line.length) return "${level.ordinal.toChar()} "

            return "${level.ordinal.toChar()}${line.substring(messageStart)}"
        }
    }
}

fun String.blockIPsIfNeeded(): String {
    return if(ConfigManager.shouldBlockIPs()){
        this.replace(ipRegex, "IP HIDDEN")
    }else{
        this
    }
}