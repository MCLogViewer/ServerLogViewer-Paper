package fish.crafting.logviewer.util

import java.util.regex.Pattern

object AnsiUtil {

    private val ANSI_REGEX = Regex("\\u001B\\[[;\\d]*m")

    fun stripAnsiCodes(text: String): String {
        return ANSI_REGEX.replace(text, "")
    }

    fun ansiToMiniMessage(text: String): String {
        val sb = StringBuilder()
        val p = Pattern.compile("\u001B\\[([0-9;]*)m")
        val m = p.matcher(text)
        var lastEnd = 0

        while (m.find()) {
            sb.append(text, lastEnd, m.start())
            val codes = m.group(1).split(";")
            val tags = StringBuilder()

            for (code in codes) {
                when (code) {
                    "0" -> tags.append("<reset>")
                    "1" -> tags.append("<bold>")
                    "3" -> tags.append("<italic>")
                    "4" -> tags.append("<underlined>")
                    "9" -> tags.append("<strikethrough>")
                    "30" -> tags.append("<black>")
                    "31" -> tags.append("<red>")
                    "32" -> tags.append("<green>")
                    "33" -> tags.append("<yellow>")
                    "34" -> tags.append("<blue>")
                    "35" -> tags.append("<light_purple>")
                    "36" -> tags.append("<aqua>")
                    "37" -> tags.append("<white>")
                    "90" -> tags.append("<dark_gray>")
                    "91" -> tags.append("<red>")
                    "92" -> tags.append("<green>")
                    "93" -> tags.append("<yellow>")
                    "94" -> tags.append("<blue>")
                    "95" -> tags.append("<light_purple>")
                    "96" -> tags.append("<aqua>")
                    "97" -> tags.append("<white>")
                }
            }

            sb.append(tags)
            lastEnd = m.end()
        }

        sb.append(text.substring(lastEnd))
        return sb.toString()
    }
}