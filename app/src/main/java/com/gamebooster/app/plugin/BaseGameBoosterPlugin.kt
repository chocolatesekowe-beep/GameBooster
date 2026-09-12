package com.gamebooster.app.plugin

import android.content.Context

/**
 * Abstract base class providing default implementations for plugins.
 * Extend this instead of implementing GameBoosterPlugin directly.
 */
abstract class BaseGameBoosterPlugin : GameBoosterPlugin {
    protected var context: Context? = null
    protected var isOptimizationActive = false

    override fun onInitialize(context: Context): Boolean {
        this.context = context
        return true
    }

    override fun onBoostStart(gamePackageName: String, boostProfile: String) {
        // Default: no action required
    }

    override fun onBoostStop(gamePackageName: String) {
        // Default: no action required
    }

    override fun getMetrics(): Map<String, String> {
        return emptyMap()
    }

    override fun applyOptimization(profile: String): Boolean {
        isOptimizationActive = true
        return true
    }

    override fun revertOptimization(): Boolean {
        isOptimizationActive = false
        return true
    }

    override fun hasRequiredPermissions(context: Context): Boolean {
        // Subclasses override this if permissions are needed
        return true
    }

    override fun getRequiredPermissions(): List<String> {
        return emptyList()
    }

    override fun onDestroy() {
        context = null
        isOptimizationActive = false
    }

    override fun onConfigurationChanged() {
        // Default: no action required
    }
}
