package com.pseddev.playstreak.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.PieceFavoriteDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceFavorite
import com.pseddev.playstreak.data.entities.PieceOrTechnique

@Database(
    entities = [PieceOrTechnique::class, Activity::class, PieceFavorite::class], 
    version = 2, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pieceOrTechniqueDao(): PieceOrTechniqueDao
    abstract fun activityDao(): ActivityDao
    abstract fun pieceFavoriteDao(): PieceFavoriteDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new statistics columns to pieces_techniques table
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN dateCreated INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN practiceCount INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN performanceCount INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN lastPracticeDate INTEGER
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN lastPerformanceDate INTEGER
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN totalPracticeDuration INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN averagePracticeLevel REAL
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN lastThreePractices TEXT
                """)
                database.execSQL("""
                    ALTER TABLE pieces_techniques ADD COLUMN lastThreePerformances TEXT
                """)
                
                // Create new piece_favorites table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS piece_favorites (
                        pieceOrTechniqueId INTEGER NOT NULL,
                        dateAdded INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                        PRIMARY KEY(pieceOrTechniqueId)
                    )
                """)
                
                // Migrate existing favorites to new table
                database.execSQL("""
                    INSERT INTO piece_favorites (pieceOrTechniqueId, dateAdded)
                    SELECT id, ${System.currentTimeMillis()}
                    FROM pieces_techniques 
                    WHERE isFavorite = 1
                """)
                
                // Populate statistics from existing activities
                populateStatisticsFromActivities(database)
                
                // Remove isFavorite column (SQLite doesn't support DROP COLUMN directly)
                // We'll create a temporary table, copy data, drop old table, rename new table
                database.execSQL("""
                    CREATE TABLE pieces_techniques_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        dateCreated INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                        practiceCount INTEGER NOT NULL DEFAULT 0,
                        performanceCount INTEGER NOT NULL DEFAULT 0,
                        lastPracticeDate INTEGER,
                        lastPerformanceDate INTEGER,
                        totalPracticeDuration INTEGER NOT NULL DEFAULT 0,
                        averagePracticeLevel REAL,
                        lastUpdated INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                        lastThreePractices TEXT,
                        lastThreePerformances TEXT
                    )
                """)
                
                database.execSQL("""
                    INSERT INTO pieces_techniques_new (
                        id, name, type, dateCreated, practiceCount, performanceCount,
                        lastPracticeDate, lastPerformanceDate, totalPracticeDuration,
                        averagePracticeLevel, lastUpdated, lastThreePractices, lastThreePerformances
                    )
                    SELECT 
                        id, name, type, dateCreated, practiceCount, performanceCount,
                        lastPracticeDate, lastPerformanceDate, totalPracticeDuration,
                        averagePracticeLevel, lastUpdated, lastThreePractices, lastThreePerformances
                    FROM pieces_techniques
                """)
                
                database.execSQL("DROP TABLE pieces_techniques")
                database.execSQL("ALTER TABLE pieces_techniques_new RENAME TO pieces_techniques")
            }
        }
        
        private fun populateStatisticsFromActivities(database: SupportSQLiteDatabase) {
            // Update practice statistics
            database.execSQL("""
                UPDATE pieces_techniques 
                SET 
                    practiceCount = (
                        SELECT COUNT(*) 
                        FROM activities 
                        WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                        AND activities.activityType = 'PRACTICE'
                    ),
                    performanceCount = (
                        SELECT COUNT(*) 
                        FROM activities 
                        WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                        AND activities.activityType = 'PERFORMANCE'
                    ),
                    lastPracticeDate = (
                        SELECT MAX(timestamp) 
                        FROM activities 
                        WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                        AND activities.activityType = 'PRACTICE'
                    ),
                    lastPerformanceDate = (
                        SELECT MAX(timestamp) 
                        FROM activities 
                        WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                        AND activities.activityType = 'PERFORMANCE'
                    ),
                    totalPracticeDuration = (
                        SELECT COALESCE(SUM(CASE WHEN minutes > 0 THEN minutes ELSE 0 END), 0)
                        FROM activities 
                        WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                        AND activities.activityType = 'PRACTICE'
                    ),
                    averagePracticeLevel = (
                        SELECT AVG(CAST(level AS REAL))
                        FROM activities 
                        WHERE activities.pieceOrTechniqueId = pieces_techniques.id 
                        AND activities.activityType = 'PRACTICE'
                        AND level > 0
                    ),
                    lastUpdated = ${System.currentTimeMillis()}
            """)
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "playstreak_database"
                ).addMigrations(MIGRATION_1_2)
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
}