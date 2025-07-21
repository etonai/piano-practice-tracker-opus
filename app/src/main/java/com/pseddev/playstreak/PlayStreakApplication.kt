package com.pseddev.playstreak

import android.app.Application
import com.pseddev.playstreak.data.AppDatabase
import com.pseddev.playstreak.data.repository.PianoRepository

class PlayStreakApplication : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        PianoRepository(
            database.pieceOrTechniqueDao(),
            database.activityDao()
        ) 
    }
    
    override fun onCreate() {
        super.onCreate()
    }
}