package fish.crafting.logviewer.packet

import fish.crafting.logviewer.ServerLogViewer
import fish.crafting.logviewer.packet.c2s.C2SFinishedStitchingPacket
import fish.crafting.logviewer.packet.c2s.C2SHandshakeConfirmPacket
import fish.crafting.logviewer.packet.c2s.C2SRequestHandshakePacket
import fish.crafting.logviewer.packet.s2c.handshake.S2CHandshakeStartPacket
import fish.crafting.logviewer.packet.s2c.log.S2CInitiateLogPacket
import fish.crafting.logviewer.packet.s2c.log.S2CLogContentsPacket
import fish.crafting.logviewer.packet.s2c.log.S2CLogLinesPacket
import fish.crafting.logviewer.packet.s2c.log.S2CSetupStatePacket
import fish.crafting.logviewer.packet.s2c.log.S2CPluginIndexPacket
import fish.crafting.logviewer.packet.s2c.log.S2CRegisterPluginsPacket
import fish.crafting.logviewer.packet.s2c.log.S2CStopConnectionPacket
import org.bukkit.Bukkit

object PacketManager {

    init {
        registerOutgoing(
            S2CLogContentsPacket,
            S2CInitiateLogPacket,
            S2CLogLinesPacket,
            S2CHandshakeStartPacket,
            S2CPluginIndexPacket,
            S2CRegisterPluginsPacket,
            S2CSetupStatePacket,
            S2CStopConnectionPacket)

        registerIncoming(
            C2SHandshakeConfirmPacket,
            C2SFinishedStitchingPacket,
            C2SRequestHandshakePacket
        )
    }

    private fun registerOutgoing(vararg packets: OutPacket) {
        val messenger = Bukkit.getServer().messenger

        for (packet in packets) {
            messenger.registerOutgoingPluginChannel(ServerLogViewer.plugin, packet.compiledChannel)
        }
    }

    private fun registerIncoming(vararg packets: InPacket){
        val messenger = Bukkit.getServer().messenger

        for (packet in packets) {
            messenger.registerIncomingPluginChannel(ServerLogViewer.plugin, packet.compiledChannel, packet)
        }
    }

    fun disable(){
        Bukkit.getServer().messenger.unregisterOutgoingPluginChannel(ServerLogViewer.plugin)
        Bukkit.getServer().messenger.unregisterIncomingPluginChannel(ServerLogViewer.plugin)
    }

}