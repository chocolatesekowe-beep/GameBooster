package com.gamebooster.app.plugin

import android.content.Context
import android.util.Log
import java.io.File
import java.lang.reflect.Method
import java.net.URLClassLoader
import kotlin.reflect.KClass

/**
 * Manages plugin lifecycle: loading, initialization, execution, and unloading.
 * Plugins are loaded from external JAR files or as bundled classes.
 */
class PluginManager(private val context: Context) {
    companion object {
        private const val TAG = "PluginManager"
        private const val PLUGINS_DIR = "plugins"
    }

    private val loadedPlugins = mutableMapOf<String, GameBoosterPlugin>()
    private val pluginMetadata = mutableMapOf<String, PluginMetadata>()

    /**
     * Initialize plugin manager and load available plugins.
     */
    fun initialize() {
        Log.d(TAG, "Initializing PluginManager")
        loadBundledPlugins()
        loadExternalPlugins()
        initializeLoadedPlugins()
    }

    /**
     * Load plugins bundled with the app (as built-in classes).
     */
    private fun loadBundledPlugins() {
        try {
            // Load built-in plugins
            val builtInPlugins = listOf(
                "com.gamebooster.app.plugin.builtin.NetworkMonitorPlugin",
                "com.gamebooster.app.plugin.builtin.ThermalManagementPlugin",
                "com.gamebooster.app.plugin.builtin.MemoryOptimizationPlugin",
                "com.gamebooster.app.plugin.builtin.BatteryOptimizationPlugin"
            )

            builtInPlugins.forEach { className ->
                try {
                    val pluginClass = Class.forName(className) as Class<GameBoosterPlugin>
                    val plugin = pluginClass.getDeclaredConstructor().newInstance()
                    registerPlugin(plugin)
                    Log.d(TAG, "Loaded bundled plugin: ${plugin.getPluginId()}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load bundled plugin $className: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bundled plugins: ${e.message}")
        }
    
    }

    /**
     * Load plugins from external JAR files in the plugins directory.
     */
    private fun loadExternalPlugins() {
        try {
            val pluginsDir = File(context.filesDir, PLUGINS_DIR)
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs()
            }

            pluginsDir.listFiles { file -> file.extension == "jar" }
                ?.forEach { jarFile ->
                    try {
                        loadPluginFromJar(jarFile)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load plugin from ${jarFile.name}: ${e.message}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading external plugins: ${e.message}")
        }
    }

    /**
     * Load a single plugin from a JAR file.
     */
    private fun loadPluginFromJar(jarFile: File) {
        val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), javaClass.classLoader)
        val jarClassesDir = File.createTempDir()

        // Extract and load plugin classes from JAR
        // This is a simplified approach; production code would use proper JAR parsing
        Log.d(TAG, "Loading plugin from JAR: ${jarFile.name}")
    }

    /**
     * Register a plugin instance.
     */
    private fun registerPlugin(plugin: GameBoosterPlugin) {
        val pluginId = plugin.getPluginId()
        if (loadedPlugins.containsKey(pluginId)) {
            Log.w(TAG, "Plugin with ID $pluginId is already registered. Skipping.")
            return
        }
        loadedPlugins[pluginId] = plugin
        pluginMetadata[pluginId] = PluginMetadata(
            id = pluginId,
            name = plugin.getPluginName(),
            version = plugin.getPluginVersion(),
            description = plugin.getPluginDescription(),
            author = plugin.getAuthor(),
            isActive = false
        )
    }

    /**
     * Initialize all loaded plugins.
     */
    private fun initializeLoadedPlugins() {
        loadedPlugins.forEach { (pluginId, plugin) ->
            try {
                val success = plugin.onInitialize(context)
                if (success) {
                    Log.d(TAG, "Plugin initialized: $pluginId")
                } else {
                    Log.w(TAG, "Plugin initialization returned false: $pluginId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing plugin $pluginId: ${e.message}")
            }
        }
    }

    /**
     * Get a plugin by its ID.
     */
    fun getPlugin(pluginId: String): GameBoosterPlugin? {
        return loadedPlugins[pluginId]
    }

    /**
     * Get all loaded plugins.
     */
    fun getAllPlugins(): List<GameBoosterPlugin> {
        return loadedPlugins.values.toList()
    }

    /**
     * Get metadata for all loaded plugins.
     */
    fun getPluginMetadata(): List<PluginMetadata> {
        return pluginMetadata.values.toList()
    }

    /**
     * Trigger boost start event for all active plugins.
     */
    fun onBoostStart(gamePackageName: String, profile: String) {
        loadedPlugins.forEach { (pluginId, plugin) ->
            try {
                plugin.onBoostStart(gamePackageName, profile)
                pluginMetadata[pluginId]?.isActive = true
                Log.d(TAG, "Boost started for plugin: $pluginId")
            } catch (e: Exception) {
                Log.e(TAG, "Error in onBoostStart for plugin $pluginId: ${e.message}")
            }
        }
    }

    /**
     * Trigger boost stop event for all active plugins.
     */
    fun onBoostStop(gamePackageName: String) {
        loadedPlugins.forEach { (pluginId, plugin) ->
            try {
                plugin.onBoostStop(gamePackageName)
                pluginMetadata[pluginId]?.isActive = false
                Log.d(TAG, "Boost stopped for plugin: $pluginId")
            } catch (e: Exception) {
                Log.e(TAG, "Error in onBoostStop for plugin $pluginId: ${e.message}")
            }
        }
    }

    /**
     * Collect metrics from all loaded plugins.
     */
    fun collectMetrics(): Map<String, Map<String, String>> {
        val allMetrics = mutableMapOf<String, Map<String, String>>()
        loadedPlugins.forEach { (pluginId, plugin) ->
            try {
                val metrics = plugin.getMetrics()
                if (metrics.isNotEmpty()) {
                    allMetrics[pluginId] = metrics
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting metrics from plugin $pluginId: ${e.message}")
            }
        }
        return allMetrics
    }

    /**
     * Apply optimization profile to all plugins.
     */
    fun applyOptimization(profile: String): Boolean {
        var allSuccessful = true
        loadedPlugins.forEach { (pluginId, plugin) ->
            try {
                val success = plugin.applyOptimization(profile)
                if (!success) {
                    Log.w(TAG, "Plugin $pluginId failed to apply optimization for profile: $profile")
                    allSuccessful = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying optimization in plugin $pluginId: ${e.message}")
                allSuccessful = false
            }
        }
        return allSuccessful
    }

    /**
     * Revert optimizations from all plugins.
     */
    fun revertOptimization(): Boolean {
        var allSuccessful = true
        loadedPlugins.forEach { (pluginId, plugin) ->
            try {
                val success = plugin.revertOptimization()
                if (!success) {
                    Log.w(TAG, "Plugin $pluginId failed to revert optimization")
                    allSuccessful = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reverting optimization in plugin $pluginId: ${e.message}")
                allSuccessful = false
            }
        }
        return allSuccessful
    }

    /**
     * Unload all plugins and cleanup resources.
     */
    fun shutdown() {
        Log.d(TAG, "Shutting down PluginManager")
        loadedPlugins.forEach { (pluginId, plugin) ->
            try {
                plugin.onDestroy()
                Log.d(TAG, "Plugin destroyed: $pluginId")
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying plugin $pluginId: ${e.message}")
            }
        }
        loadedPlugins.clear()
        pluginMetadata.clear()
    }
}

/**
 * Data class for plugin metadata.
 */
data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    var isActive: Boolean = false
)
