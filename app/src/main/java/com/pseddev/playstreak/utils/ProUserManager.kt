package com.pseddev.playstreak.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages Pro user status using SharedPreferences for local storage.
 * This is a simple implementation for development and testing purposes.
 * In production, this would integrate with Google Play Billing or similar service.
 */
class ProUserManager private constructor(context: Context) {
    
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Check if the current user is a Pro subscriber
     * @return true if user has Pro status, false for free user
     */
    fun isProUser(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PRO_USER, false)
    }
    
    /**
     * Set the Pro user status
     * @param isPro true to grant Pro status, false for free user
     */
    fun setProUser(isPro: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PRO_USER, isPro)
            .apply()
    }
    
    /**
     * Toggle Pro user status (useful for testing)
     * @return the new Pro status after toggling
     */
    fun toggleProStatus(): Boolean {
        val newStatus = !isProUser()
        setProUser(newStatus)
        return newStatus
    }
    
    /**
     * Reset Pro status to free user (useful for testing)
     */
    fun resetToFreeUser() {
        setProUser(false)
    }
    
    /**
     * Check if user can add more favorites based on Pro status and current count
     * @param currentFavoriteCount the current number of favorites the user has
     * @return true if user can add more favorites, false if at limit
     */
    fun canAddMoreFavorites(currentFavoriteCount: Int): Boolean {
        return if (isProUser()) {
            true // Pro users have unlimited favorites
        } else {
            currentFavoriteCount < FREE_USER_FAVORITE_LIMIT // Free users: only if under 4
        }
    }
    
    companion object {
        private const val PREFS_NAME = "playstreak_pro_prefs"
        private const val KEY_IS_PRO_USER = "is_pro_user"
        const val FREE_USER_FAVORITE_LIMIT = 4
        
        @Volatile
        private var INSTANCE: ProUserManager? = null
        
        fun getInstance(context: Context): ProUserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProUserManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}