# Piano Practice Tracker - Complete Implementation Plan

## Project Overview
Android app for tracking piano practice sessions and performances with local storage and Google Drive sync capability.

## Technical Stack
- **Language**: Kotlin
- **UI Framework**: Android View System (XML layouts)
- **Database**: Room (SQLite)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Core Features
1. Track practice sessions and performances
2. Differentiate between pieces and techniques
3. Track practice streaks
4. View progress through multiple interfaces (dashboard, calendar, timeline, piece-focused)
5. Import/export CSV data
6. Sync with Google Drive
7. Manage favorite pieces

## Database Schema

### Entities

```kotlin
@Entity(tableName = "pieces_techniques")
data class PieceOrTechnique(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: ItemType, // PIECE or TECHNIQUE
    val isFavorite: Boolean = false
)

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // Store as milliseconds
    val pieceOrTechniqueId: Long,
    val activityType: ActivityType, // PRACTICE or PERFORMANCE
    val level: Int, // 1-4 for practice, 1-3 for performance
    val performanceType: String, // "practice", "online", or "live"
    val minutes: Int = -1, // -1 when not tracked
    val notes: String = ""
)

enum class ItemType { PIECE, TECHNIQUE }
enum class ActivityType { PRACTICE, PERFORMANCE }
```

### DAOs

```kotlin
@Dao
interface PieceOrTechniqueDao {
    @Query("SELECT * FROM pieces_techniques ORDER BY name ASC")
    fun getAllPiecesAndTechniques(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<PieceOrTechnique>>
    
    @Query("SELECT * FROM pieces_techniques WHERE type = :type ORDER BY name ASC")
    fun getByType(type: ItemType): Flow<List<PieceOrTechnique>>
    
    @Insert
    suspend fun insert(piece: PieceOrTechnique): Long
    
    @Update
    suspend fun update(piece: PieceOrTechnique)
    
    @Query("DELETE FROM pieces_techniques")
    suspend fun deleteAll()
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE pieceOrTechniqueId = :pieceId ORDER BY timestamp DESC")
    fun getActivitiesForPiece(pieceId: Long): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getActivitiesForDateRange(startTime: Long, endTime: Long): Flow<List<Activity>>
    
    @Insert
    suspend fun insert(activity: Activity)
    
    @Query("DELETE FROM activities")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(DISTINCT date(timestamp/1000, 'unixepoch', 'localtime')) as streak FROM activities WHERE timestamp >= :startTime")
    suspend fun getStreakCount(startTime: Long): Int
}
```

## Application Structure

### Package Structure
```
com.yourname.pianotracker/
├── MainActivity.kt
├── PianoTrackerApplication.kt
├── data/
│   ├── AppDatabase.kt
│   ├── entities/
│   │   ├── PieceOrTechnique.kt
│   │   └── Activity.kt
│   ├── daos/
│   │   ├── PieceOrTechniqueDao.kt
│   │   └── ActivityDao.kt
│   └── repository/
│       └── PianoRepository.kt
├── ui/
│   ├── main/
│   │   ├── MainFragment.kt
│   │   └── MainViewModel.kt
│   ├── addactivity/
│   │   ├── AddActivityFragment.kt
│   │   ├── AddActivityViewModel.kt
│   │   ├── SelectPieceFragment.kt
│   │   ├── AddNewPieceFragment.kt
│   │   ├── SelectLevelFragment.kt
│   │   ├── TimeInputFragment.kt
│   │   ├── NotesInputFragment.kt
│   │   └── SummaryFragment.kt
│   ├── progress/
│   │   ├── ViewProgressFragment.kt
│   │   ├── DashboardFragment.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── TimelineFragment.kt
│   │   ├── TimelineViewModel.kt
│   │   ├── CalendarFragment.kt
│   │   ├── CalendarViewModel.kt
│   │   ├── PiecesFragment.kt
│   │   └── PiecesViewModel.kt
│   ├── favorites/
│   │   ├── FavoritesFragment.kt
│   │   ├── FavoritesViewModel.kt
│   │   └── FavoritesAdapter.kt
│   └── importexport/
│       ├── ImportExportFragment.kt
│       └── ImportExportViewModel.kt
└── utils/
    ├── CsvHandler.kt
    ├── StreakCalculator.kt
    ├── DateUtils.kt (future)
    └── GoogleDriveHelper.kt (future)
```

## Dependencies (build.gradle)

```gradle
dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Navigation
    implementation "androidx.navigation:navigation-fragment-ktx:2.7.6"
    implementation "androidx.navigation:navigation-ui-ktx:2.7.6"
    
    // Room
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    // ViewModel
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    
    // Calendar View
    implementation 'com.github.prolificinteractive:material-calendarview:2.0.1'
    
    // Google Drive
    implementation 'com.google.android.gms:play-services-auth:20.7.0'
    implementation 'com.google.api-client:google-api-client-android:2.2.0'
    implementation 'com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0'
    
    // CSV handling
    implementation 'com.opencsv:opencsv:5.8'
}
```

## Key Implementation Details

### 1. Streak Calculation Logic
```kotlin
class StreakCalculator {
    fun calculateCurrentStreak(activities: List<Activity>): Int {
        if (activities.isEmpty()) return 0
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        var streak = 0
        var currentDate = today.clone() as Calendar
        
        while (true) {
            val dayStart = currentDate.timeInMillis
            val dayEnd = currentDate.clone().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            
            val hasActivity = activities.any { 
                it.timestamp >= dayStart && it.timestamp < dayEnd 
            }
            
            if (hasActivity) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else if (currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1) {
                // Yesterday had no activity, check if we're still in grace period (today)
                break
            } else {
                break
            }
        }
        
        return streak
    }
}
```

### 2. CSV Format
```
DateTime,Length,ActivityType,Piece,Level,PerformanceType,Notes
2024-11-21 14:30:00,-1,PRACTICE,Chopin Etude Op.10,3,practice,
2024-11-21 10:15:00,15,PRACTICE,Scales - C Major,2,practice,
2024-11-20 19:45:00,-1,PERFORMANCE,Bach Invention No.8,3,live,Great performance!
```

### 3. Calendar Color Scheme
- **No activity**: White (#FFFFFF)
- **Practice only (1-3 activities)**: Light Blue (#B3D9FF)
- **Practice only (4-8 activities)**: Medium Blue (#66B2FF)
- **Practice only (9+ activities)**: Dark Blue (#0066CC)
- **Performance day (1-3 activities)**: Light Green (#B3FFB3)
- **Performance day (4-8 activities)**: Medium Green (#66FF66)
- **Performance day (9+ activities)**: Dark Green (#00CC00)

### 4. Icon Resources
- Piece icon: Use musical note (🎵) - `@drawable/ic_music_note`
- Technique icon: Use gear/settings (⚙️) - `@drawable/ic_settings`

### 5. Business Rules
- All activities are logged with current timestamp only
- No editing or deletion of activities once saved
- Time tracking is optional and only available for:
  - Practice Level 2 (Incomplete)
  - Any technique (regardless of level)
- Techniques cannot be selected for performances
- CSV import replaces all existing data
- Google Drive sync uses Drive data as source of truth in conflicts
- Streak resets after one full calendar day with no activities

### 6. Navigation Flow
```
Main Menu
├── View Progress
│   ├── Dashboard (default tab)
│   ├── Calendar
│   ├── Pieces (select piece → view history)
│   └── Timeline
├── Add Activity
│   ├── Select Type (Practice/Performance)
│   ├── Select Piece/Technique (filtered based on type)
│   ├── Add New (if needed)
│   ├── Select Level
│   ├── Time Input (if Level 2 or Technique)
│   ├── Notes Input (if Performance)
│   └── Summary → Save → Return to Main
├── Manage Favorites
│   └── Toggle favorites → Save
└── Import/Export
    ├── Sync with Drive
    ├── Export CSV
    └── Import CSV
```

### 7. Sync Dialog Implementation
- Show on app startup
- Simple Yes/No dialog
- If Yes: perform sync, show progress
- If No: continue to main screen
- Store last sync timestamp in SharedPreferences

### 8. Error Handling
- Network errors during sync: Show toast, continue offline
- CSV parsing errors: Show specific error message
- Database errors: Log and show generic error message
- Invalid input: Prevent at UI level (e.g., empty piece names)

### 9. Testing Considerations
- Test streak calculation across timezone changes
- Test CSV import with malformed data
- Test sync with no network connection
- Test with large datasets (1000+ activities)

### 10. UI/UX Guidelines
- Use Material Design components
- Consistent back navigation
- Loading indicators for all async operations
- Confirmation only for destructive actions (CSV import)
- Toast messages for successful operations

## Implementation Order

1. **Phase 1 - Core Database & Basic UI** ✅ **COMPLETED**
   - ✅ Set up Room database
   - ✅ Create main activity and navigation
   - ✅ Implement Add Activity flow (practice and performance)
   - ✅ Streak calculation and business logic

2. **Phase 2 - View Progress Module** ✅ **COMPLETED**
   - ✅ ViewProgressFragment with ViewPager2 and TabLayout
   - ✅ Dashboard tab - Today/yesterday summary with streak
   - ✅ Timeline tab - Chronological activity list with RecyclerView
   - ✅ Calendar tab - Date selection with activity display
   - ✅ Pieces tab - Individual piece statistics and analytics
   - ✅ Comprehensive ViewModels with reactive data flows

3. **Phase 3 - Favorites Management** ✅ **COMPLETED**
   - ✅ Favorites toggle functionality
   - ✅ Visual indicators in UI
   - ✅ Enhanced favorites management screen

4. **Phase 4 - Data Exchange** ✅ **COMPLETED**
   - ✅ CSV export functionality with datetime timestamps
   - ✅ CSV import functionality with comprehensive validation
   - ✅ File picker integration using Storage Access Framework
   - ✅ ImportExportFragment with progress indication
   - ✅ CsvHandler utility with manual CSV processing
   - ✅ Race condition fixes for proper stream management
   - ✅ Favorites preservation during import operations

5. **Phase 5 - Google Drive** (Future)
   - Google Sign-In
   - Drive API integration
   - Sync functionality
   - Startup sync dialog

## Notes for Implementation

1. Use `SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)` for CSV datetime stamps
2. Use `System.currentTimeMillis()` for timestamps
3. Always use coroutines for database operations
4. Use Flow for reactive UI updates
5. Handle configuration changes properly
6. Request necessary permissions for file access
7. Use ViewModel to survive configuration changes
8. Implement proper error handling for all external operations

## Manifest Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
```

## Strings Resources

```xml
<!-- Practice Levels -->
<string name="practice_level_1">Level 1 - Essentials</string>
<string name="practice_level_2">Level 2 - Incomplete</string>
<string name="practice_level_3">Level 3 - Complete with Review</string>
<string name="practice_level_4">Level 4 - Perfect Complete</string>

<!-- Performance Levels -->
<string name="performance_level_1">Level 1 - Failed</string>
<string name="performance_level_2">Level 2 - Unsatisfactory</string>
<string name="performance_level_3">Level 3 - Satisfactory</string>

<!-- Performance Types -->
<string name="performance_online">Online Performance</string>
<string name="performance_live">Live Performance</string>
```