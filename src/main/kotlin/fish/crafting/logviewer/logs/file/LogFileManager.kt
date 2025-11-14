package fish.crafting.logviewer.logs.file

import fish.crafting.logviewer.ServerLogViewer
import fish.crafting.logviewer.logs.runtime.LogLine
import fish.crafting.logviewer.logs.runtime.RuntimeLogManager
import fish.crafting.logviewer.packet.s2c.log.S2CInitiateLogPacket
import fish.crafting.logviewer.packet.s2c.log.S2CLogContentsPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.GZIPOutputStream
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.ceil


object LogFileManager {

    private var writtenLines = 0
    private val lock = ReentrantLock()
    private val queue = LinkedBlockingQueue<LogLine>()
    @Volatile private var running = true
    @Volatile private var pauseWrites = false
    private var acceptingIncomingLines = false

    init {
        val dataFolder = ServerLogViewer.plugin.dataFolder
        if(!dataFolder.exists()){
            dataFolder.mkdir()
        }

        purgeLogFile()
        copyMinecraftLog()

        val logFile = getLogFile()
        if (!logFile.exists()) logFile.createNewFile()
        startWriterThread()
    }

    fun getWrittenLines() = writtenLines

    fun shutdown() {
        running = false
        queue.clear()
        purgeLogFile()
    }

    private fun LogLine.logText() = if(writtenLines == 0) this.compile() else ("\n" + this.compile())

    private fun startWriterThread() {
        thread(name = "LogWriterThread", isDaemon = true) {
            var writer: BufferedWriter? = null
            try {
                writer = BufferedWriter(FileWriter(getLogFile(), true))

                while (running) {
                    val first = queue.poll(1, java.util.concurrent.TimeUnit.SECONDS)
                    if (first != null) {
                        if (!pauseWrites) {
                            writer.append(first.logText())
                            while (true) {
                                if (pauseWrites) break
                                val next = queue.poll() ?: break
                                writer.append(next.logText())
                            }

                            writer.flush()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    writer?.close()
                } catch (_: IOException) {}
            }
        }
    }

    private fun purgeLogFile() {
        lock.withLock {
            pauseWrites = true
            getLogFile().writeText("")
            pauseWrites = false
        }
    }

    private fun copyMinecraftLog() {
        lock.withLock {
            pauseWrites = true
            writtenLines = 0
            queue.clear()
            acceptingIncomingLines = false

            getMinecraftLogFile().inputStream().use { input ->
                getLogFile().outputStream().use { output ->
                    BufferedReader(InputStreamReader(input)).useLines { lines ->
                        var first = true
                        for (line in lines) {
                            if(line.endsWith(RuntimeLogManager.uniqueLogStarter)) break

                            val converted = LogLine.convertPrePluginLine(line)
                            val text = if(first) {
                                first = false
                                converted
                            }else{
                                "\n" + converted
                            }

                            output.write(text.toByteArray())
                            writtenLines++
                        }

                        acceptingIncomingLines = true
                    }
                }
            }

            pauseWrites = false
        }
    }

    fun addLogLine(line: LogLine){
        if(!acceptingIncomingLines) return
        queue.add(line)
    }

    fun sendLogs(player: Player){
        pauseWrites = true
        send(player, getLogFile())
        pauseWrites = false
    }

    fun getLogFile(): File {
        return File(ServerLogViewer.plugin.dataFolder, "latest_log")
    }

    fun getMinecraftLogFile(): File {
        return File(File(Bukkit.getServer().worldContainer, "logs"), "latest.log")
    }

    fun send(player: Player, file: File): Boolean {
        val compressedLog = compressLog(file) ?: return false
        val chunkSize = 1024000 // / 1048576

        val parts = ceil(compressedLog.size / chunkSize.toDouble()).toInt()

        val logId = ServerLogViewer.latestLogId
        S2CInitiateLogPacket.send(player, parts, logId)

        var i = 0
        var index = 0
        while (i < compressedLog.size) {
            val end = compressedLog.size.coerceAtMost(i + chunkSize)
            val chunk = compressedLog.copyOfRange(i, end)

            sendChunkToPlayer(player, chunk, index++, logId)
            i += chunkSize
        }

        return true
    }

    private fun sendChunkToPlayer(player: Player, chunk: ByteArray, index: Int, logId: String) {
        S2CLogContentsPacket.send(player, chunk, index, logId)
    }

    fun compressLog(file: File): ByteArray? {
        lock.withLock {
            file.inputStream().use { fis ->
                ByteArrayOutputStream().use { os ->
                    GZIPOutputStream(os).use { gzipOut ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while ((fis.read(buffer).also { read = it }) != -1) {
                            gzipOut.write(buffer, 0, read)
                        }

                        gzipOut.finish()
                        return os.toByteArray()
                    }
                }
            }
        }
    }

    private fun runAsync(runnable: () -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(ServerLogViewer.plugin, runnable)
    }
}