package com.pseddev.playstreak.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique

@Database(
    entities = [PieceOrTechnique::class, Activity::class], 
    version = 1, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pieceOrTechniqueDao(): PieceOrTechniqueDao
    abstract fun activityDao(): ActivityDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "playstreak_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromItemType(type: ItemType): String = type.name
    
    @TypeConverter
    fun toItemType(type: String): ItemType = ItemType.valueOf(type)
    
    @TypeConverter
    fun fromActivityType(type: ActivityType): String = type.name
    
    @TypeConverter
    fun toActivityType(type: String): ActivityType = ActivityType.valueOf(type)
}