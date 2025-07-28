package com.pseddev.playstreak.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized configuration manager for all app-wide preferences.
 * Handles pruning settings, lifetime counters, and other configuration options.
 */
class ConfigurationManager private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Check if data pruning is enabled by the user
     * @return true if pruning is allowed, false for safety (default)
     */
    fun isPruningEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_ALLOW_PRUNING, false)
    }
    
    /**
     * Set whether data pruning is allowed
     * @param enabled true to allow pruning, false to disable for safety
     */
    fun setPruningEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_ALLOW_PRUNING, enabled)
            .apply()
    }
    
    /**
     * Get the lifetime activity count
     * @return total activities ever created across app lifetime
     */
    fun getLifetimeActivityCount(): Int {
        return sharedPreferences.getInt(KEY_LIFETIME_ACTIVITIES, 0)
    }
    
    /**
     * Set the lifetime activity count
     * @param count total activities ever created
     */
    fun setLifetimeActivityCount(count: Int) {
        sharedPreferences.edit()
            .putInt(KEY_LIFETIME_ACTIVITIES, count)
            .apply()
    }
    
    /**
     * Increment the lifetime activity counter
     * @param increment number to add to lifetime count (default 1)
     */
    fun incrementLifetimeActivityCount(increment: Int = 1) {
        val current = getLifetimeActivityCount()
        setLifetimeActivityCount(current + increment)
    }
    
    /**
     * Initialize lifetime counter for existing users
     * @param currentActivityCount the current number of stored activities
     */
    fun initializeLifetimeCounter(currentActivityCount: Int) {
        if (!sharedPreferences.getBoolean(KEY_COUNTER_INITIALIZED, false)) {
            // For existing users, initialize with current count
            setLifetimeActivityCount(currentActivityCount)
            sharedPreferences.edit()
                .putBoolean(KEY_COUNTER_INITIALIZED, true)
                .apply()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "playstreak_configuration"
        private const val KEY_ALLOW_PRUNING = "allow_data_pruning"
        private const val KEY_LIFETIME_ACTIVITIES = "lifetime_activities_count"
        private const val KEY_COUNTER_INITIALIZED = "lifetime_counter_initialized"
        
        @Volatile
        private var INSTANCE: ConfigurationManager? = null
        
        fun getInstance(context: Context): ConfigurationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigurationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}