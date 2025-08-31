package com.pseddev.playstreak.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.AchievementDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.Achievement
import com.pseddev.playstreak.data.entities.AchievementType
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique

@Database(
    entities = [PieceOrTechnique::class, Activity::class, Achievement::class], 
    version = 4, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pieceOrTechniqueDao(): PieceOrTechniqueDao
    abstract fun activityDao(): ActivityDao
    abstract fun achievementDao(): AchievementDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Migration", "Starting migration from version 1 to 2")
                
                try {
                    // Add new columns to pieces_techniques table
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN dateCreated INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN practiceCount INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN performanceCount INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN lastPracticeDate INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN secondLastPracticeDate INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN thirdLastPracticeDate INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN lastPerformanceDate INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN secondLastPerformanceDate INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN thirdLastPerformanceDate INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN lastSatisfactoryPractice INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN lastSatisfactoryPerformance INTEGER")
                    database.execSQL("ALTER TABLE pieces_techniques ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT 0")
                    
                    // Set dateCreated and lastUpdated to current timestamp for existing pieces
                    val currentTime = System.currentTimeMillis()
                    database.execSQL("UPDATE pieces_techniques SET dateCreated = $currentTime WHERE dateCreated = 0")
                    database.execSQL("UPDATE pieces_techniques SET lastUpdated = $currentTime WHERE lastUpdated = 0")
                    
                    // Populate practice counts
                    database.execSQL("""
                        UPDATE pieces_techniques 
                        SET practiceCount = (
                            SELECT COUNT(*) 
                            FROM activities 
                            WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                            AND activities.activityType = 'PRACTICE'
                        )
                    """)
                    
                    // Populate performance counts
                    database.execSQL("""
                        UPDATE pieces_techniques 
                        SET performanceCount = (
                            SELECT COUNT(*) 
                            FROM activities 
                            WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                            AND activities.activityType = 'PERFORMANCE'
                        )
                    """)
                    
                    // Populate last practice date
                    database.execSQL("""
                        UPDATE pieces_techniques 
                        SET lastPracticeDate = (
                            SELECT MAX(timestamp) 
                            FROM activities 
                            WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                            AND activities.activityType = 'PRACTICE'
                        )
                    """)
                    
                    // Populate last performance date
                    database.execSQL("""
                        UPDATE pieces_techniques 
                        SET lastPerformanceDate = (
                            SELECT MAX(timestamp) 
                            FROM activities 
                            WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                            AND activities.activityType = 'PERFORMANCE'
                        )
                    """)
                    
                    // Populate last satisfactory practice (level >= 4)
                    database.execSQL("""
                        UPDATE pieces_techniques 
                        SET lastSatisfactoryPractice = (
                            SELECT MAX(timestamp) 
                            FROM activities 
                            WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                            AND activities.activityType = 'PRACTICE'
                            AND activities.level >= 4
                        )
                    """)
                    
                    // Populate last satisfactory performance (level >= 3)
                    database.execSQL("""
                        UPDATE pieces_techniques 
                        SET lastSatisfactoryPerformance = (
                            SELECT MAX(timestamp) 
                            FROM activities 
                            WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                            AND activities.activityType = 'PERFORMANCE'
                            AND activities.level >= 3
                        )
                    """)
                    
                    Log.d("Migration", "Migration from version 1 to 2 completed successfully")
                } catch (e: Exception) {
                    Log.e("Migration", "Error during migration from version 1 to 2", e)
                    throw e
                }
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Migration", "Starting migration from version 2 to 3")
                
                try {
                    // Create achievements table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS achievements (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            type TEXT NOT NULL,
                            title TEXT NOT NULL,
                            description TEXT NOT NULL,
                            iconEmoji TEXT NOT NULL,
                            isUnlocked INTEGER NOT NULL DEFAULT 0,
                            unlockedAt INTEGER,
                            dateCreated INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)
                    
                    Log.d("Migration", "Migration from version 2 to 3 completed successfully")
                } catch (e: Exception) {
                    Log.e("Migration", "Error during migration from version 2 to 3", e)
                    throw e
                }
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Migration", "Starting migration from version 3 to 4")
                
                try {
                    // Recreate achievements table with type as primary key to prevent duplicates
                    database.execSQL("DROP TABLE IF EXISTS achievements")
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS achievements (
                            type TEXT PRIMARY KEY NOT NULL,
                            title TEXT NOT NULL,
                            description TEXT NOT NULL,
                            iconEmoji TEXT NOT NULL,
                            isUnlocked INTEGER NOT NULL DEFAULT 0,
                            unlockedAt INTEGER,
                            dateCreated INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)
                    
                    Log.d("Migration", "Migration from version 3 to 4 completed successfully")
                } catch (e: Exception) {
                    Log.e("Migration", "Error during migration from version 3 to 4", e)
                    throw e
                }
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "playstreak_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
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
    
    @TypeConverter
    fun fromAchievementType(type: AchievementType): String = type.name
    
    @TypeConverter
    fun toAchievementType(type: String): AchievementType = AchievementType.valueOf(type)
}