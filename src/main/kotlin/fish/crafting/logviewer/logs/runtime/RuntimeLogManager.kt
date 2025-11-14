package fish.crafting.logviewer.logs.runtime

import fish.crafting.fish.crafting.logviewer.util.send
import fish.crafting.logviewer.ServerLogViewer
import fish.crafting.logviewer.config.ConfigManager
import fish.crafting.logviewer.logs.LogLevel
import fish.crafting.logviewer.logs.file.LogFileManager
import fish.crafting.logviewer.logs.runtime.LogLine.Companion.ipRegex
import fish.crafting.logviewer.packet.s2c.log.S2CPluginIndexPacket
import fish.crafting.logviewer.packet.s2c.log.S2CRegisterPluginsPacket
import fish.crafting.logviewer.player.PlayerManager
import fish.crafting.logviewer.player.getMemory
import fish.crafting.logviewer.util.AnsiUtil
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader
import net.kyori.adventure.text.Component
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.layout.PatternLayout
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue


object RuntimeLogManager {

    private val log4jAppender: Appender?
    private var pluginInitialized = false
    private val index = LogIndex()
    private var initializedBeginIndex = false
    val uniqueLogStarter = generateLogStarter()
    private val bootstrapLines = ConcurrentLinkedQueue<BootstrapLineData>()

    init {
        val coreLogger = LogManager.getRootLogger() as Logger

        val layout = PatternLayout.newBuilder()
            .withPattern("[%d{HH:mm:ss} %level]: %msg%n")
            .build()

        coreLogger.info("Injecting ServerLogViewer...")

        log4jAppender = object : AbstractAppender("PaperLogInterceptor", null, layout, false, null) {
            override fun append(event: LogEvent) {
                val line = event.message.formattedMessage
                val level = event.level
                val pluginName = getLastPluginCaller()

                if(!pluginInitialized) {
                    bootstrapLines.add(BootstrapLineData(line, level, pluginName))
                    return
                }

                handleIncomingLine(line, level, pluginName)
            }
        }

        //Since we can't track logs before the plugin is started,
        //we will print a unique string to our logs.
        //This will store the unique string in the latest.log file,
        //from which this plugin will read lines before the unique
        //string.
        //The reason this must be done specifically is that file
        //reading is unreliable when it comes to syncing values from memory.
        coreLogger.info(uniqueLogStarter)

        (log4jAppender as AbstractAppender).start()
        coreLogger.addAppender(log4jAppender)
    }

    private fun handleIncomingLine(line: String, level: Level, pluginName: String?, isSplitAlready: Boolean = false){
        //Some lines may have \n in them, but they count as one line according to log4j. When they are appended to the file
        //however, they will count as multiple lines and then the indices will be offset. So we separate them here and treat them as each line
        if(!isSplitAlready && line.contains("\n")){
            val split = line.split("\n")
            for (newLine in split) {
                handleIncomingLine(newLine, level, pluginName, isSplitAlready = true)
            }

            return
        }

        val plugin = pluginName?.let { index.getPlugin(it) } ?: -1
        handleLogLine(line, LogLevel.fromLog4j(level), plugin)
    }

    fun onPluginInitialized(){
        val iterator = bootstrapLines.iterator()
        while(iterator.hasNext()){
            val (line, level, pluginName) = iterator.next()
            handleIncomingLine(line, level, pluginName)
            iterator.remove()
        }

        pluginInitialized = true

        if(!bootstrapLines.isEmpty()){ //How
            onPluginInitialized()
            bootstrapLines.clear()
        }

    }

    fun disable() {
        if (log4jAppender != null) {
            (LogManager.getRootLogger() as Logger)
                .removeAppender(log4jAppender)
        }
    }

    fun handleLogLine(line: String, level: LogLevel, plugin: Short){
        val formatted = AnsiUtil.stripAnsiCodes(line).blockIPsIfNeeded()
        val logLine = LogLine(formatted, level, plugin)

        if(!initializedBeginIndex){
            index.markIndexBegin(LogFileManager.getWrittenLines())
            initializedBeginIndex = true
        }

        index.add(plugin)
        LogFileManager.addLogLine(logLine)

        PlayerManager.forEachSLVUser {
            it.getMemory().handleLogLine(it, ServerLogViewer.latestLogId, logLine)
        }
    }

    fun getLastPluginCaller(): String? {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk {

                val list = it.map { frame ->
                    if(frame.className.startsWith("fish.crafting.logviewer.logs.runtime.RuntimeLogManager")) return@map null
                    if(frame.className.startsWith("me.lucko.spark")) return@map "Spark"
                    //Some plugins somehow don't route through their class loader, so we need to define them in here :P
                    //TODO eventually move this to some sort of online database of plugins
                    if(frame.className.startsWith("me.lucko.luckperms")) return@map "LuckPerms"

                    val classLoader = frame.declaringClass.getClassLoader()
                    if(classLoader is ConfiguredPluginClassLoader){
                        classLoader.plugin?.name
                    }else{
                        null
                    }
                }.filter { e -> e != null }.toList()

                if (list.isEmpty()) return@walk "Paper"
                list.last()
            }
    }

    fun handleNewPlugin(name: String, id: Short) {
        if(!pluginInitialized) return

        PlayerManager.forEachSLVUser { player ->
            S2CRegisterPluginsPacket.send(player, name, id)
        }
    }

    fun sendPluginsAndIndex(player: Player) {
        S2CRegisterPluginsPacket.send(player, index.getPlugins())
        S2CPluginIndexPacket.send(player, index)
    }

    private fun generateLogStarter(): String {
        return "SLV" + UUID.randomUUID().toString() + UUID.randomUUID().toString()
    }

    private data class BootstrapLineData(val line: String, val level: Level, val pluginName: String?)
}