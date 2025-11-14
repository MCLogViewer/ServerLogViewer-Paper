package fish.crafting.logviewer

import fish.crafting.logviewer.logs.runtime.RuntimeLogManager
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap

class SLVBootstrapper : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        val load = RuntimeLogManager
    }
}