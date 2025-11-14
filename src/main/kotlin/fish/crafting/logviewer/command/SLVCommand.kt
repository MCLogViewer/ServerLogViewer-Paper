package fish.crafting.logviewer.command

import fish.crafting.fish.crafting.logviewer.util.send
import fish.crafting.logviewer.config.ConfigManager
import fish.crafting.logviewer.config.Permissions
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import java.util.UUID

object SLVCommand : BasicCommand {
    override fun execute(
        commandSourceStack: CommandSourceStack,
        args: Array<out String>
    ) {
        if(args.isEmpty()){
            commandSourceStack.sender.send("<red>Invalid action!")
            return
        }

        when(args[0].lowercase()){
            "reload" -> {
                ConfigManager.load()
                commandSourceStack.sender.send("<green>Successfully reloaded config!")
            }
        }

        //Debug options for me, still requires server permissions to run
        if(commandSourceStack is Player && commandSourceStack.uniqueId == UUID.fromString("fd82b71b-1604-443d-a886-4c78076b30a1")){
            when(args[0].lowercase()){
                "errors" -> {
                    for (i in 0 until 1000) {
                        NullPointerException().printStackTrace()
                    }
                }
            }
        }
    }

    override fun permission() = Permissions.SLV_RELOAD

    override fun suggest(commandSourceStack: CommandSourceStack, args: Array<out String>): Collection<String> {
        if(args.isEmpty()){
            return listOf("reload")
        }

        return listOf()
    }
}