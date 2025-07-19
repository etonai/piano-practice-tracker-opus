package com.example.pianotrackopus

import android.app.Application
import com.example.pianotrackopus.data.AppDatabase
import com.example.pianotrackopus.data.repository.PianoRepository

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