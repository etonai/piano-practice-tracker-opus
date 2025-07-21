# Bug Tracking

This document tracks known bugs, their status, and resolutions for the Piano Track Opus app.

## Status Legend
- 🐛 **Open** - Bug is confirmed and needs fixing
- 🔄 **In Progress** - Bug is being worked on
- ✅ **Fixed** - Bug has been resolved
- ❌ **Closed** - Bug closed without fix (e.g., not reproducible, won't fix)

## Known Bugs

### Bug #1: 🔄 Calendar Month Swiping Should Be Disabled
**Status:** In Progress  
**Date Reported:** 2025-07-20  
**Severity:** Medium  

**Description:**  
Users can swipe left/right on the calendar to change months, but this should be disabled. Month navigation should only be possible through the Previous/Next buttons in the header.

**Steps to Reproduce:**  
1. Open the Calendar view
2. Swipe left or right on the calendar grid
3. Calendar changes months

**Expected Behavior:**  
Swiping should not change months. Only the Previous/Next buttons should control month navigation.

**Actual Behavior:**  
Calendar responds to swipe gestures and changes months.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Disabled calendar month swiping while preserving day selection functionality:
- **Touch Event Handling**: Added custom `OnTouchListener` to intercept horizontal swipe gestures
- **Swipe Detection**: Tracks horizontal movement from initial touch position
- **Threshold**: Blocks horizontal swipes greater than 30 pixels to prevent accidental month changes
- **Day Selection Preserved**: Allows small movements and vertical touches for normal day selection
- **Button Navigation**: Previous/Next month buttons remain fully functional

**Technical Implementation:**
- **Method**: `disableCalendarSwiping()` in CalendarFragment
- **Logic**: Intercepts `ACTION_MOVE` events and consumes horizontal swipes beyond threshold
- **Touch Flow**: `ACTION_DOWN` stores initial X position, `ACTION_MOVE` checks horizontal distance
- **Selective Blocking**: Only blocks significant horizontal movements (>30px) while allowing day taps

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

**Previous Attempts:**  
- Attempted fix: `android:nestedScrollingEnabled="false"` in XML layout
- Attempted fix: Touch event interception - **REVERTED** (blocked day selection)
- Current status: Day selection restored, but month swiping may still work
- **Update 2025-07-20**: Confirmed that when a day is selected in the calendar, users can still swipe left/right to change months. This swiping should be completely disabled - only Previous/Next buttons should control month navigation.
- **Implementation Attempt**: Added custom touch handler with 30px threshold - **PARTIAL SUCCESS** (reduces but doesn't completely eliminate month swiping. User reports occasional successful swipes still occur inconsistently)
- **Next Steps**: User will test further to identify repeatable reproduction case for the remaining swipe behavior

---

### Bug #2: ❌ Calendar Has Too Much Spacing Between Numbers
**Status:** Closed  
**Date Reported:** 2025-07-20  
**Date Closed:** 2025-07-20  
**Severity:** Low  

**Description:**  
The calendar view has excessive spacing between the day numbers, both vertically (between rows) and horizontally (between columns). This makes the calendar appear sparse and wastes screen space.

**Steps to Reproduce:**  
1. Open the Calendar view
2. Observe the spacing between day numbers

**Expected Behavior:**  
Calendar should have compact spacing between day numbers for better visual density and screen space utilization.

**Actual Behavior:**  
Calendar has large gaps between day numbers making it appear spread out.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Closure Reason:**  
Closed without implementing - current calendar spacing is acceptable for readability and touch targets. Previous attempts to reduce spacing caused readability issues. The current layout provides good balance between visual density and usability.

---

### Bug #3: ✅ Pieces Sort Line Too Long - Needs Text Optimization
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
The pieces sort line text is too long and should be shortened. Specifically "activities" should be changed to "activity" and "Last Date" should be changed to "Date" to reduce line length.

**Steps to Reproduce:**  
1. Open the Pieces view
2. Observe the sort line text length

**Expected Behavior:**  
Sort line should use shorter text: "activity" instead of "activities" and "Date" instead of "Last Date".

**Actual Behavior:**  
Sort line uses longer text that may cause wrapping or display issues.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Resolution:**  
Applied text changes to reduce line length:
- "activities" → "activity"  
- "Last Date" → "Date"

**Files Modified:**
- `app/src/main/res/layout/fragment_pieces.xml`

---

### Bug #4: ✅ Missing Buffer Space at Bottom of Tabs
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Medium  

**Description:**  
The Home, Calendar, and Pieces tabs need buffer space (empty whitespace) at the bottom to allow users to scroll past the last content item. This prevents floating action buttons or other UI elements from covering text or content near the bottom of the screen.

**Steps to Reproduce:**  
1. Open any of the Home, Calendar, or Pieces tabs
2. Scroll to the bottom of the content
3. Observe that there's no extra space to scroll past the last item

**Expected Behavior:**  
User should be able to scroll down into empty whitespace below the last content item to ensure all content is visible and not covered by UI elements.

**Actual Behavior:**  
Content ends abruptly at the last item with no buffer space, potentially causing UI elements to cover important content.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Resolution:**  
Added buffer space to all three main tabs:
- **Home tab (Dashboard)**: Added 100dp buffer View at bottom of ScrollView
- **Calendar tab**: Added 100dp buffer View at bottom of ScrollView  
- **Pieces tab**: Added 108dp bottom padding to RecyclerView

This allows users to scroll past the last content item to ensure floating action buttons don't cover important content.

**Files Modified:**
- `app/src/main/res/layout/fragment_dashboard.xml`
- `app/src/main/res/layout/fragment_calendar.xml`
- `app/src/main/res/layout/fragment_pieces.xml`

---

### Bug #6: ✅ Activity Sort Should Default to Descending
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
When sorting activities, the default sort order should be descending (newest first) rather than ascending. This provides a better user experience as users typically want to see their most recent activities at the top.

**Steps to Reproduce:**  
1. Open any view that displays activities with sorting
2. Observe the default sort order

**Expected Behavior:**  
Activities should be sorted in descending order by default (newest/highest values first).

**Actual Behavior:**  
Activities are sorted in ascending order by default (oldest/lowest values first).

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Resolution:**  
Implemented smart defaults for sorting based on sort type:
- **PiecesViewModel**: Date and Activity Count sorting now default to descending (newest/highest first)
- **AbandonedViewModel**: Changed default to descending (most recently abandoned first)
- **Alphabetical sorting**: Remains ascending (A-Z) as expected

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/PiecesViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/AbandonedViewModel.kt`

---

### Bug #7: ✅ Calendar Day Circles Too Small
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
The circles around the day numbers in the calendar view are too small, making them harder to see and interact with. The circles should be larger for better visibility and touch targets.

**Steps to Reproduce:**  
1. Open the Calendar view
2. Observe the size of the circles around the day numbers

**Expected Behavior:**  
Calendar day circles should be larger for better visibility and easier touch interaction.

**Actual Behavior:**  
Calendar day circles are too small, making them harder to see and tap accurately.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Increased calendar day circle size for better visibility and touch interaction:
- **Size Change**: Increased from 32dp x 32dp to 40dp x 40dp (25% larger)
- **Container Adjustment**: Increased calendar day container height from 48dp to 52dp to accommodate larger circles
- **Touch Target**: Larger circles provide better touch targets for easier interaction
- **Visual Impact**: More prominent day numbers for improved readability
- **Layout**: Maintains proper spacing and prevents overlap between adjacent days

**Files Modified:**
- `app/src/main/res/layout/calendar_day_layout.xml`

---

### Bug #8: ✅ Home Page Activities Missing Level Information
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Medium  

**Description:**  
The activities listed on the Home page do not include the activity level information, unlike the Calendar page which shows level details (e.g., "Practice (3)", "Performance (2)"). This inconsistency makes the Home page less informative.

**Steps to Reproduce:**  
1. Open the Home page and view the activity list
2. Open the Calendar page and select a date with activities
3. Compare the activity information displayed

**Expected Behavior:**  
Home page activities should include level information in the same format as the Calendar page (e.g., "Practice (3)", "Performance (2)").

**Actual Behavior:**  
Home page activities only show activity type without level information.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Resolution:**  
Added level information to both today and yesterday activity lists in DashboardFragment. Activity format now matches Calendar page: "• time - piece - type (level) minutes".

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/DashboardFragment.kt`

---

### Bug #9: ✅ Change Home Tab Label to Dashboard
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
The first tab is currently labeled "Home" but should be changed to "Dashboard" to better reflect its purpose as a dashboard view with statistics and recent activity overview.

**Steps to Reproduce:**  
1. Open the app
2. Observe the bottom navigation tab labels

**Expected Behavior:**  
The first tab should be labeled "Dashboard" instead of "Home".

**Actual Behavior:**  
The first tab is labeled "Home".

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Resolution:**  
Changed tab label from "Home" to "Dashboard" in ViewProgressFragment to better reflect the tab's purpose as a dashboard with statistics and activity overview.

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/ViewProgressFragment.kt`

---

### Bug #10: ✅ Add Suggestions Section to Dashboard Tab
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Medium  

**Description:**  
The Dashboard tab should include a suggestions section to help users decide what to practice next. It should display the oldest favorite suggestion and the oldest non-favorite suggestion. When there are ties (multiple pieces with the same "oldest" date), all tied suggestions should be listed.

**Steps to Reproduce:**  
1. Open the Dashboard tab
2. Look for practice suggestions

**Expected Behavior:**  
Dashboard should display:
- A "Suggestions" section
- The oldest favorite piece(s) that need practice
- The oldest non-favorite piece(s) that need practice  
- If multiple pieces tie for "oldest", show all tied pieces

**Actual Behavior:**  
Dashboard has no suggestions section to guide practice decisions.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Resolution:**  
Added suggestions section to Dashboard tab that displays practice recommendations. The section shows the oldest favorite and non-favorite pieces that need practice, with support for multiple tied suggestions when pieces have the same "oldest" practice date.

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/DashboardFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/DashboardViewModel.kt`

---

### Bug #11: ✅ Add Activity Dialog Navigation Issue from Dashboard/Calendar
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Medium  

**Description:**  
When adding an activity from the Dashboard or Calendar screens, the Save and Cancel buttons in the add activity dialog do not return the user to the originating screen. The Save button does successfully save the activity, but the navigation flow is broken. The add activity button from Settings works correctly.

**Steps to Reproduce:**  
1. Open Dashboard or Calendar tab
2. Tap the add activity button/option
3. Fill out the activity dialog
4. Tap Save or Cancel
5. Observe that you are not returned to the Dashboard/Calendar screen

**Expected Behavior:**  
After tapping Save or Cancel in the add activity dialog, the user should return to the screen where they originally pressed the add activity button (Dashboard or Calendar).

**Actual Behavior:**  
Save and Cancel buttons don't navigate back to the originating screen, leaving the user in an unexpected location.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Fixed add activity navigation to correctly return to the originating screen:
- **Root Cause**: SummaryFragment was hard-coded to always navigate back to mainFragment (Settings)
- **Solution**: Changed navigation to use `popBackStack(addActivityFragment, true)` which returns to the screen that launched the add activity flow
- **Behavior**: Now works correctly for all entry points:
  - From **Dashboard/Calendar**: Returns to progressFragment (Dashboard/Calendar tabs)
  - From **Settings**: Returns to mainFragment (Settings screen)
- **Fixed Actions**: Both Save and Cancel buttons now navigate back to the correct originating screen

**Technical Details:**
- **Before**: `popBackStack(mainFragment, false)` - always went to Settings
- **After**: `popBackStack(addActivityFragment, true)` - pops the entire add activity flow and returns to launching screen
- **Navigation Flow**: originating screen → addActivityFragment → ... → summaryFragment → back to originating screen

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/SummaryFragment.kt`

---

### Bug #12: ✅ Missing Delete Activity Feature in Timeline Tab
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Medium  

**Description:**  
The Timeline tab should allow users to delete activities directly from the timeline view. This would provide a convenient way to remove incorrect or unwanted activity entries without having to navigate to other screens.

**Steps to Reproduce:**  
1. Open the Timeline tab
2. View activities in the timeline
3. Look for delete/remove options for individual activities

**Expected Behavior:**  
Users should be able to delete activities from the timeline view (e.g., through long press, swipe gesture, or context menu) which would remove the activity from the database.

**Actual Behavior:**  
Timeline tab does not provide any way to delete activities from the timeline view.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Added delete functionality to Timeline tab with the following features:
- **Delete Button**: Added delete icon button to each timeline activity item in the top-right header area
- **Confirmation Dialog**: Shows confirmation dialog with activity details (piece name and date/time) before deletion
- **Database Integration**: Implemented complete delete flow from UI → ViewModel → Repository → DAO → Database
- **Real-time Updates**: Timeline automatically updates after deletion through LiveData observers
- **User Experience**: Clean delete icon with proper touch targets and Material Design styling

**Technical Implementation:**
- **Database Layer**: Added `@Delete suspend fun delete(activity: Activity)` to ActivityDao
- **Repository Layer**: Added `suspend fun deleteActivity(activity: Activity)` to PianoRepository  
- **ViewModel Layer**: Added `deleteActivity(activityWithPiece: ActivityWithPiece)` to TimelineViewModel
- **UI Layer**: Added delete button to timeline item layout with click handler in TimelineAdapter
- **Confirmation Flow**: AlertDialog shows piece name and formatted date/time before confirming deletion

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/data/daos/ActivityDao.kt`
- `app/src/main/java/com/pseddev/practicetracker/data/repository/PianoRepository.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineAdapter.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineFragment.kt`
- `app/src/main/res/layout/item_timeline_activity.xml`
- `app/src/main/res/drawable/ic_delete.xml` (new)

---

### Bug #13: ✅ Add Last Export Tracking to Import/Export Data Page
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
The Import/Export Data page should track and display when the last export was performed. This information should be shown at the bottom of the page, under the existing "Last sync" information, to help users keep track of their export history.

**Steps to Reproduce:**  
1. Open Settings
2. Navigate to Import/Export Data page
3. Look for last export information

**Expected Behavior:**  
The Import/Export Data page should display "Last export: [date/time]" information below the existing "Last sync" section to track when exports were last performed.

**Actual Behavior:**  
The page only shows "Last sync" information but does not track or display when the last export was done.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Added last export tracking functionality:
- **ViewModel**: Added SharedPreferences to track last export timestamp and `getLastExportTime()` method
- **Export Process**: Updates timestamp when CSV export completes successfully  
- **UI**: Added "Last export" TextView below "Last sync" information
- **Format**: Matches existing sync time format (MMM dd, yyyy HH:mm) or shows "Never"
- **Persistence**: Export time persists across app restarts using SharedPreferences

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/importexport/ImportExportViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/importexport/ImportExportFragment.kt`
- `app/src/main/res/layout/fragment_import_export.xml`

---

### Bug #14: ✅ Duplicate Titles on Settings, Manage Favorites, and Import/Export Data Pages
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
The Settings, Manage Favorites, and Import/Export Data pages display duplicate titles - one in the action bar/toolbar with a back arrow, and another title shown on the page content itself. This creates visual redundancy since these pages are now navigated to from other screens rather than being independent pages.

**Steps to Reproduce:**  
1. Navigate to Settings page
2. Observe both the toolbar title and page content title
3. Navigate to Manage Favorites page
4. Observe both the toolbar title and page content title
5. Navigate to Import/Export Data page
6. Observe both the toolbar title and page content title

**Expected Behavior:**  
Each page should show the title only once - either in the toolbar/action bar OR on the page content, but not both.

**Actual Behavior:**  
Pages show duplicate titles: one in the toolbar with back arrow navigation and another as a heading on the page content.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Resolution:**  
Removed duplicate title TextViews from all three pages and adjusted layout constraints and margins:
- **Settings page**: Removed "Settings" title TextView and updated button positioning
- **Manage Favorites page**: Removed "Manage Favorites" header TextView  
- **Import/Export Data page**: Removed "Import/Export Data" title TextView and updated card positioning

This eliminates visual redundancy while maintaining clean navigation with toolbar titles and back arrows.

**Files Modified:**
- `app/src/main/res/layout/fragment_main.xml`
- `app/src/main/res/layout/fragment_favorites.xml`
- `app/src/main/res/layout/fragment_import_export.xml`

---

### Bug #15: ❌ Calendar Day Selection Circle Too Small
**Status:** Closed  
**Date Reported:** 2025-07-20  
**Date Closed:** 2025-07-20  
**Severity:** Low  

**Description:**  
When selecting a day in the calendar, a small purple circle appears around the selected day. The circle is too small and should be increased by 3x for better visibility and user feedback.

**Steps to Reproduce:**  
1. Open the Calendar tab
2. Tap on any day in the calendar
3. Observe the purple selection circle around the day

**Expected Behavior:**  
The purple selection circle should be 3x larger than its current size for better visibility and clear indication of the selected day.

**Actual Behavior:**  
The purple selection circle is too small and barely visible, making it difficult to see which day is currently selected.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Closure Reason:**  
Closed as duplicate/superseded by Bug #18. The selection ring visibility has been addressed through the color change to light bright purple in Bug #18, which provides better visibility than just increasing the stroke width. The current 9dp stroke width (implemented in previous fixes) combined with the new light bright purple color provides optimal visibility.

---

### Bug #16: ✅ Calendar Activity Section Missing Date in Title
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
On the Calendar page, when displaying activities for a selected day, the section title shows information like "1 activities (Practice)" or "No activities on this date" but does not include the actual date. The title should include the selected date for better user context and clarity.

**Steps to Reproduce:**  
1. Open the Calendar tab
2. Tap on any day in the calendar
3. Observe the activity section title below the calendar

**Expected Behavior:**  
The title should include the selected date and use proper grammar, for example:
- "January 15: 1 activity (Practice)"
- "January 15: 3 activities (Mixed)"
- "January 15: No activities on this date"
- Or similar format that clearly shows the selected date with correct singular/plural grammar

**Actual Behavior:**  
The title only shows activity information without the date:
- "1 activities (Practice)"
- "No activities on this date"

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Enhanced the calendar activity section title with date information and proper grammar:
- **Date Format**: Smart formatting - "Today", "Yesterday", or "Month Day" (e.g., "January 15")
- **Grammar Fix**: Proper singular/plural - "1 activity" vs "2 activities"
- **Title Examples**: 
  - "Today: 1 activity (Practice)"
  - "Yesterday: 3 activities (Mixed)"
  - "January 15: No activities on this date"
- **Location**: CalendarFragment.kt `updateSelectedDateView()` function
- **Logic**: Added date formatting and conditional grammar based on activity count

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

---

### Bug #17: ✅ Calendar Month Navigation Issues with Selected Date
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Medium  

**Description:**  
When switching between months in the calendar while having a day selected, multiple issues occur: the activity title shows the previously selected date from the old month, the activities displayed appear to be from the first day of the new month, and no day appears visually selected in the new month's calendar view.

**Steps to Reproduce:**  
1. Open the Calendar tab
2. Select a day (e.g., July 14)
3. Navigate to a different month using Previous/Next month buttons (e.g., move to June)
4. Observe the activity section title and calendar selection state

**Expected Behavior:**  
When navigating to a new month:
- Either clear the selection and show no activities, OR
- Select the same day number in the new month if it exists (e.g., June 14), OR  
- Select the first day of the new month and update the title accordingly
- The calendar should visually show which day is selected
- The activity title should match the actually selected date

**Actual Behavior:**  
- Activity title still shows the old month's date (e.g., "July 14: ...")
- Activities displayed appear to be from a different date (e.g., June 1)
- No day appears visually selected in the calendar
- Inconsistency between displayed date and actual data

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Fixed calendar month navigation to maintain consistent date selection:
- **Smart Day Selection**: When switching months, attempts to select the same day number in the new month (e.g., July 14 → June 14)
- **Edge Case Handling**: If the day doesn't exist in the new month (e.g., Feb 30), selects the last day of the month
- **State Synchronization**: Updates both `selectedDate` variable and ViewModel selection to keep them in sync
- **Visual Update**: Calls `notifyCalendarChanged()` to refresh the calendar display and show the new selection
- **Fallback**: If no previous selection exists, defaults to the first day of the month

**Logic Flow**:
1. User switches month (Previous/Next buttons)
2. System attempts to maintain same day number in new month
3. Updates `selectedDate` variable and ViewModel state
4. Refreshes calendar view to show visual selection
5. Activity title and data now match the correctly selected date

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

---

### Bug #5: ✅ Duplicate Piece Entries from CSV Import
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** High  

**Description:**  
When importing CSV data, duplicate entries for "Somewhere That's Green" appeared - one with 20 activities and one with 2 activities.

**Root Cause:**  
CSV contained different Unicode apostrophe characters:
- Regular apostrophe (') U+0027
- Curly apostrophe (') U+2019

**Resolution:**  
- Created `TextNormalizer` utility class
- Implemented comprehensive Unicode normalization for all text input
- Applied normalization to import, export, and manual entry workflows
- Used Unicode escape sequences for reliable character replacement

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/utils/TextNormalizer.kt` (new)
- `app/src/main/java/com/pseddev/practicetracker/utils/CsvHandler.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/AddActivityViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/pieces/AddPieceViewModel.kt`

---

### Bug #18: ✅ Calendar Selection Ring Color Too Dark
**Status:** Fixed  
**Date Reported:** 2025-07-20  
**Date Fixed:** 2025-07-20  
**Severity:** Low  

**Description:**  
The calendar selection ring around the selected date should be changed to a light bright shade of purple for better visual appeal and consistency with the app's design theme.

**Steps to Reproduce:**  
1. Open the Calendar tab
2. Tap on any day in the calendar
3. Observe the color of the selection ring around the selected day

**Expected Behavior:**  
The selection ring should be a light bright shade of purple that provides good visibility while being visually appealing.

**Actual Behavior:**  
The current selection ring color may not match the desired light bright purple shade.

**Environment:**  
- App Version: 1.0.8
- All Android devices

**Implementation Details:**  
Updated calendar selection ring to use a light bright purple color:
- **Color**: Changed from dark purple (`#FF6200EE`) to light bright purple (`#FFCC99FF`)
- **Visibility**: Light purple provides better contrast against calendar backgrounds
- **Consistency**: Maintains 9dp stroke width from previous Bug #15 fix
- **User Experience**: More visually appealing and easier to see selection state

**Technical Implementation:**
- **Color Resource**: Added `calendar_selection_ring` color (#FFCC99FF) to colors.xml
- **Calendar Selection**: Updated CalendarFragment to use new color resource instead of purple_500
- **Stroke Application**: Maintains existing 9dp stroke width with new light bright purple color

**Files Modified:**
- `app/src/main/res/values/colors.xml`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

---

## Bug Report Template

When reporting new bugs, please use this template:

```markdown
### 🐛 [Bug Title]
**Status:** Open  
**Date Reported:** YYYY-MM-DD  
**Severity:** [Critical/High/Medium/Low]  
**Reporter:** [Name/Username]

**Description:**  
[Clear description of the bug]

**Steps to Reproduce:**  
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Behavior:**  
[What should happen]

**Actual Behavior:**  
[What actually happens]

**Environment:**  
- App Version: [e.g., 1.0.8]
- Android Version: [e.g., Android 13]
- Device: [e.g., Pixel 7]

**Additional Information:**  
[Any other relevant details, screenshots, logs, etc.]
```

---

## Fixed Bugs Archive

This section contains bugs that have been resolved and can serve as reference for similar issues in the future.

### Text Normalization Issues
**Common Cause:** Unicode character variations (apostrophes, quotes, whitespace)  
**Solution Pattern:** Implement comprehensive text normalization using Unicode escape sequences  
**Prevention:** Always normalize user input at entry points (manual input, import, etc.)