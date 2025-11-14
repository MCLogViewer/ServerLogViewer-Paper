package fish.crafting.logviewer.logs

import org.apache.logging.log4j.Level


enum class LogLevel() {

    SYSTEM,
    INFO,
    WARN,
    ERROR
    ;

    companion object {
        fun fromLog4j(lvl: Level): LogLevel {
            if(lvl == Level.ERROR || lvl == Level.FATAL) return ERROR
            if(lvl == Level.WARN) return WARN
            if(lvl == Level.INFO) return INFO

            return SYSTEM
        }
    }

}