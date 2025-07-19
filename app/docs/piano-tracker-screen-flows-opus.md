# Piano Practice Tracker - Detailed Screen Flows

*Last Updated: July 2025*

**Implementation Status:**
- ✅ Main Menu (0000) - COMPLETED
- ✅ View Progress Module (1000-1400) - COMPLETED
- ✅ Add Activity Flow (2000-2700) - COMPLETED  
- ✅ Manage Favorites (3000) - COMPLETED
- ✅ Import/Export Data (4000) - COMPLETED

## 0000 - Main Menu
```
┌─────────────────────────────┐
│  Piano Practice Tracker     │
│                             │
│  Current Streak: 12 days 🔥 │
│                             │
│  ┌───────────────────────┐  │
│  │   View Progress      │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │   Add Activity       │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │  Manage Favorites    │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │  Import/Export Data  │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 1000 - View Progress
```
┌─────────────────────────────┐
│  ← View Progress           │
│                             │
│  ┌─────────────────────┐   │
│  │ Dashboard │Calendar │   │
│  │ Pieces │ Timeline  │   │
│  └─────────────────────┘   │
│                             │
│  [Content changes based     │
│   on selected tab]          │
└─────────────────────────────┘
```

### 1100 - Dashboard Tab
```
┌─────────────────────────────┐
│  Current Streak: 12 days 🔥 │
│  ─────────────────────────  │
│  Today (3 activities):      │
│  🎵 Chopin Etude - Level 3  │
│  🎵 Bach Invention - Perf.  │
│  ⚙️ Scales C Major - 15min  │
│                             │
│  Yesterday (2 activities):  │
│  🎵 Moonlight Son. - Level 2│
│  ⚙️ Arpeggios - 20min       │
└─────────────────────────────┘
```
Icons: 🎵 = Piece, ⚙️ = Technique

### 1200 - Calendar Tab
```
┌─────────────────────────────┐
│      November 2024          │
│  Su Mo Tu We Th Fr Sa      │
│              1  2  3        │
│   4  5  6  7  8  9 10      │
│  11 12 13 14 15 16 17      │
│  18 19 20 21 22 23 24      │
│  25 26 27 28 29 30         │
│                             │
│  Legend:                    │
│  ⬜ No activity             │
│  🟦 Practice only (1-3)     │
│  🟦 Practice only (4-8)     │
│  🟦 Practice only (9+)      │
│  🟩 Performance day (1-3)   │
│  🟩 Performance day (4-8)   │
│  🟩 Performance day (9+)    │
└─────────────────────────────┘
```

### 1300 - Pieces Tab
```
┌─────────────────────────────┐
│  Select Piece/Technique:    │
│  ┌───────────────────────┐  │
│  │ 🎵 Bach Invention No.8│  │
│  │ 🎵 Chopin Etude Op.10 │  │
│  │ ⚙️ Scales - C Major   │  │
│  │ ⚙️ Arpeggios          │  │
│  └───────────────────────┘  │
│                             │
│  [Shows history when        │
│   piece is selected]        │
└─────────────────────────────┘
```

### 1400 - Timeline Tab
```
┌─────────────────────────────┐
│  Nov 21, 2024               │
│  • 2:30 PM - Chopin Etude   │
│    Practice Level 3         │
│  • 10:15 AM - Scales        │
│    Technique - 15 min       │
│                             │
│  Nov 20, 2024               │
│  • 7:45 PM - Bach Invention │
│    Live Performance ★★★     │
│  • 3:00 PM - Moonlight Son. │
│    Practice Level 2         │
└─────────────────────────────┘
```

## 2000 - Add Activity
```
┌─────────────────────────────┐
│  ← Add Activity            │
│                             │
│  What type of activity?     │
│                             │
│  ┌───────────────────────┐  │
│  │     Practice         │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │    Performance       │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 2100 - Select Piece/Technique
```
┌─────────────────────────────┐
│  ← Select Piece            │
│                             │
│  ┌───────────────────────┐  │
│  │ + Add New             │  │
│  └───────────────────────┘  │
│                             │
│  Favorites:                 │
│  • 🎵 Chopin Etude Op.10    │
│  • 🎵 Moonlight Sonata      │
│                             │
│  All Pieces/Techniques:     │
│  • ⚙️ Arpeggios             │
│  • 🎵 Bach Invention No.8   │
│  • 🎵 Chopin Etude Op.10    │
│  • 🎵 Moonlight Sonata      │
│  • ⚙️ Scales - C Major      │
└─────────────────────────────┘
```
Note: Only pieces shown if Performance was selected

## 2110 - Add New Piece/Technique
```
┌─────────────────────────────┐
│  ← Add New                 │
│                             │
│  Name:                      │
│  ┌───────────────────────┐  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  Type:                      │
│  ○ Piece  ○ Technique      │
│                             │
│  ┌───────────────────────┐  │
│  │        OK             │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 2200 - Select Practice Level
```
┌─────────────────────────────┐
│  ← Select Level            │
│                             │
│  Chopin Etude Op.10         │
│                             │
│  ○ Level 1 - Essentials    │
│  ○ Level 2 - Incomplete    │
│  ○ Level 3 - Complete      │
│    with Review             │
│  ○ Level 4 - Perfect       │
│    Complete                │
│                             │
│  ┌───────────────────────┐  │
│  │       Continue        │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 2210 - Time Input (Level 2 or Technique)
```
┌─────────────────────────────┐
│  ← Practice Time           │
│                             │
│  How many minutes?          │
│  (Optional)                 │
│                             │
│  ┌───────────────────────┐  │
│  │         15            │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │       Continue        │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │    Skip (No Time)     │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 3200 - Select Performance Level
```
┌─────────────────────────────┐
│  ← Performance Level       │
│                             │
│  Bach Invention No.8        │
│  Live Performance           │
│                             │
│  ○ Level 1 - Failed        │
│  ○ Level 2 - Unsatisfactory│
│  ○ Level 3 - Satisfactory  │
│                             │
│  ┌───────────────────────┐  │
│  │       Continue        │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 3210 - Performance Notes
```
┌─────────────────────────────┐
│  ← Add Notes (Optional)    │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │                       │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │       Continue        │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │    Skip (No Notes)    │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 2300/3300 - Summary
```
┌─────────────────────────────┐
│  Summary                    │
│                             │
│  Piece: Chopin Etude Op.10  │
│  Type: Practice             │
│  Level: 3 - Complete w/Rev  │
│  Time: Not recorded         │
│  Date: Nov 21, 2024 2:30 PM │
│                             │
│  ┌───────────────────────┐  │
│  │         Save          │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │        Cancel         │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 4000 - Manage Favorites
```
┌─────────────────────────────┐
│  ← Manage Favorites        │
│                             │
│  Tap to toggle favorite:    │
│                             │
│  ⭐ 🎵 Chopin Etude Op.10   │
│  ⭐ 🎵 Moonlight Sonata     │
│  ☆ 🎵 Bach Invention No.8  │
│  ☆ ⚙️ Scales - C Major     │
│  ☆ ⚙️ Arpeggios            │
│                             │
│  ⭐ = Favorite              │
│  ☆ = Not Favorite          │
│                             │
│  ┌───────────────────────┐  │
│  │        Done           │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

## 4000 - Import/Export Data ✅ IMPLEMENTED
```
┌─────────────────────────────┐
│  ← Import/Export Data      │
│                             │
│  ┌───────────────────────┐  │
│  │   Sync with Drive     │  │
│  │   (Not Implemented)   │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │   Export to CSV       │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │   Import from CSV     │  │
│  └───────────────────────┘  │
│                             │
│  ⚠️ Warning: Importing CSV  │
│  data will replace all      │
│  existing data in the app.  │
│                             │
│  [Progress Bar when active] │
└─────────────────────────────┘
```

### CSV Export Flow:
1. User taps "Export to CSV"
2. Android file picker opens
3. User selects save location and filename
4. Export proceeds with progress indication
5. Success/error message displayed

### CSV Import Flow:
1. User taps "Import from CSV"
2. Warning message appears about data replacement
3. Android file picker opens for CSV selection
4. Confirmation dialog shows before import
5. Import proceeds with validation and error reporting
6. Favorites are preserved for matching piece names
7. Success message shows number of imported activities

## Sync Dialog (On App Start)
```
┌─────────────────────────────┐
│     Sync with Drive?        │
│                             │
│  Would you like to sync     │
│  with Google Drive?         │
│                             │
│  ┌─────────┬─────────────┐  │
│  │   Yes   │     No      │  │
│  └─────────┴─────────────┘  │
└─────────────────────────────┘
```

## Color Specifications

### Calendar Colors (RGB values)
Practice Only:
- Light Blue (1-3): #B3D9FF
- Medium Blue (4-8): #66B2FF  
- Dark Blue (9+): #0066CC

Performance Days:
- Light Green (1-3): #B3FFB3
- Medium Green (4-8): #66FF66
- Dark Green (9+): #00CC00