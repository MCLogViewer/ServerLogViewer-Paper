package fish.crafting.logviewer.logs.runtime

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.shorts.ShortArrayList

class LogIndex {

    private val pluginIndex = ShortArrayList()
    private val plugins = hashMapOf<String, Short>()
    private var indexBegin = 0

    fun getPlugin(name: String): Short {
        val preExisting = plugins[name]
        if(preExisting != null) return preExisting

        val id = (if(plugins.size > Short.MAX_VALUE) -1 else plugins.size).toShort()
        plugins[name] = id

        RuntimeLogManager.handleNewPlugin(name, id)
        return id
    }

    fun getPlugin(line: Int): Short {
        if(outsideOfBounds(line)) return -1
        return pluginIndex.getShort(line - indexBegin)
    }

    fun markIndexBegin(index: Int){
        indexBegin = index
    }

    fun outsideOfBounds(line: Int) = line < indexBegin || (line - indexBegin) >= pluginIndex.size

    fun add(plugin: Short) {
        pluginIndex.add(plugin)
    }

    fun getIndices() = pluginIndex
    fun getPlugins() = plugins
    fun getIndexBegin() = indexBegin

}