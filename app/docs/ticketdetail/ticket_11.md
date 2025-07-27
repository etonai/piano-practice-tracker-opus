# Ticket #11: Add Export Pieces TO CSV and Import Activities from CSV

## Overview
Add two new export/import features to the Import/Export Data screen to provide more granular data management options.

## Current State
The Import/Export Data screen currently has:
- **Export to CSV** - Exports all activities with piece information
- **Import from CSV** - Replaces ALL data (activities and pieces)
- Google Drive sync functionality (disabled)
- Purge all data (debug only)

## Requested Features

### 1. Export Pieces to CSV
**Button Label:** "Export Pieces to CSV"  
**Functionality:** Export piece statistics and information to a separate CSV file

**Data to Include:**
- Piece ID
- Piece Name  
- Type (Piece/Technique)
- Date Created
- Practice Count
- Performance Count
- Last Practice Date
- Last Performance Date
- Total Practice Duration (minutes)
- Average Practice Level
- Last Updated timestamp
- Last Three Practices (JSON or readable format)
- Last Three Performances (JSON or readable format)
- Is Favorite (boolean)

**File Naming:** `PlayStreak_pieces_export_YYYY-MM-DD_HHMMSS.csv`

### 2. Import Activities from CSV
**Button Label:** "Import Activities from CSV"  
**Functionality:** Import only activity data without replacing existing pieces

**Behavior:**
- Import activities from CSV file
- Create missing pieces automatically (if referenced in activities)
- Preserve existing pieces and their statistics
- Update piece statistics based on imported activities
- Show validation and confirmation dialogs
- Provide detailed import results with counts

**Validation:**
- Check for valid activity data format
- Ensure piece references are valid or can be created
- Validate activity types, dates, levels, etc.
- Respect user limits (activity count, piece count)

## Technical Implementation

### Backend Changes
1. **Repository Methods:**
   - Add `exportPiecesToCsv(writer: Writer)` method
   - Add `importActivitiesOnlyFromCsv(reader: Reader)` method

2. **Utility Classes:**
   - Extend `PieceStatisticsCsvExporter` for pieces-only export
   - Create `ActivityOnlyImporter` for selective activity import

3. **Data Validation:**
   - Create validation for activity-only imports
   - Ensure piece creation/lookup logic

### Frontend Changes
1. **ImportExportFragment:**
   - Add two new buttons with appropriate styling
   - Add corresponding activity result contracts
   - Add coroutine handling for async operations

2. **ImportExportViewModel:**
   - Add methods for piece export and activity-only import
   - Add LiveData for new operation results
   - Handle loading states and error reporting

3. **UI Layout:**
   - Update `fragment_import_export.xml` to include new buttons
   - Maintain consistent styling with existing buttons

## User Experience

### Export Pieces Flow
1. User taps "Export Pieces to CSV"
2. File picker appears with suggested filename
3. User selects save location
4. Export completes with success/error feedback
5. File contains comprehensive piece statistics

### Import Activities Flow
1. User taps "Import Activities from CSV"
2. File picker appears for CSV selection
3. Validation occurs with feedback
4. Confirmation dialog shows what will be imported
5. Import proceeds with detailed results
6. Piece statistics automatically updated

## Benefits
- **Granular Control:** Users can export pieces separately from activities
- **Safer Imports:** Import activities without losing existing piece data
- **Data Analysis:** Separate piece statistics file for analysis
- **Backup Flexibility:** More options for partial data backup/restore

## Acceptance Criteria
- [ ] Export Pieces to CSV button creates comprehensive piece statistics file
- [ ] Import Activities from CSV preserves existing pieces while adding activities
- [ ] Both operations provide clear feedback and error handling
- [ ] File naming conventions are consistent with existing exports
- [ ] Import validation prevents data corruption
- [ ] Piece statistics are automatically updated after activity import
- [ ] UI layout is consistent with existing import/export buttons

## Priority: Medium
This feature enhances data management capabilities but is not critical for core app functionality.

## Estimated Effort: Medium
Requires backend repository changes, utility class extensions, and frontend UI additions with proper validation and error handling.