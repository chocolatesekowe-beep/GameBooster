package com.gamebooster.app.plugin

import android.content.Context

/**
 * Base interface for all GameBooster plugins.
 * Plugins extend functionality without modifying core app code.
 *
 * Plugins must:
 * - Have a no-arg constructor
 * - Implement all required methods
 * - Handle errors gracefully
 * - Not access restricted APIs without proper permissions
 */
interface GameBoosterPlugin {
    /**
     * Unique identifier for this plugin (e.g., "network_monitor", "thermal_control")
     */
    fun getPluginId(): String

    /**
     * Human-readable name of the plugin
     */
    fun getPluginName(): String

    /**
     * Plugin version in semantic versioning format (e.g., "1.0.0")
     */
    fun getPluginVersion(): String

    /**
     * Brief description of plugin functionality
     */
    fun getPluginDescription(): String

    /**
     * Author or organization name
     */
    fun getAuthor(): String

    /**
     * Minimum API level required by this plugin
     */
    fun getMinimumApiLevel(): Int

    /**
     * Initialize the plugin with application context.
     * Called once when plugin is loaded.
     *
     * @param context Application context
     * @return true if initialization successful, false otherwise
     */
    fun onInitialize(context: Context): Boolean

    /**
     * Called when app enters a boost session for a specific game.
     *
     * @param gamePackageName Package name of the game being boosted
     * @param boostProfile Selected boost profile ("balanced", "performance", "battery_saver")
     */
    fun onBoostStart(gamePackageName: String, boostProfile: String)

    /**
     * Called when boost session ends.
     *
     * @param gamePackageName Package name of the game
     */
    fun onBoostStop(gamePackageName: String)

    /**
     * Get current performance metrics from this plugin.
     * Must return quickly to avoid UI lag.
     *
     * @return Map of metric name to value (e.g., mapOf("ping" to "15ms"))
     */
    fun getMetrics(): Map<String, String>

    /**
     * Apply boost optimization based on selected profile.
     *
     * @param profile Boost profile ("balanced", "performance", "battery_saver")
     * @return true if optimization applied successfully
     */
    fun applyOptimization(profile: String): Boolean

    /**
     * Revert optimizations applied by this plugin.
     *
     * @return true if revert successful
     */
    fun revertOptimization(): Boolean

    /**
     * Check if plugin has required permissions.
     *
     * @param context Application context
     * @return true if all required permissions are granted
     */
    fun hasRequiredPermissions(context: Context): Boolean

    /**
     * Get list of permissions required by this plugin.
     *
     * @return List of permission strings (e.g., ["android.permission.INTERNET"])
     */
    fun getRequiredPermissions(): List<String>

    /**
     * Cleanup resources when plugin is unloaded.
     * Called before plugin removal or app shutdown.
     */
    fun onDestroy()

    /**
     * Handle configuration changes.
     * Called when app is rotated, resized, or configuration changes.
     */
    fun onConfigurationChanged()
}
