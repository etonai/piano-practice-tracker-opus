package com.pseddev.practicetracker

import android.app.Application
import com.pseddev.practicetracker.data.AppDatabase
import com.pseddev.practicetracker.data.repository.PianoRepository

class PianoTrackerApplication : Application() {
    
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