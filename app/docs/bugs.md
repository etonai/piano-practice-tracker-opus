# Bug Tracking

This document tracks known bugs, their status, and resolutions for the Piano Track Opus app.

## Status Legend
- 🐛 **Open** - Bug is confirmed and needs fixing
- 🔄 **In Progress** - Bug is being worked on
- ✅ **Fixed** - Bug has been resolved
- ❌ **Closed** - Bug closed without fix (e.g., not reproducible, won't fix)

## Known Bugs

### Bug #1: 🐛 Calendar Month Swiping Should Be Disabled
**Status:** Open  
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

**Additional Information:**  
- Attempted fix: `android:nestedScrollingEnabled="false"` in XML layout
- Attempted fix: Touch event interception - **REVERTED** (blocked day selection)
- Current status: Day selection restored, but month swiping may still work

---

### Bug #2: 🐛 Calendar Has Too Much Spacing Between Numbers
**Status:** Open  
**Date Reported:** 2025-07-20  
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

**Additional Information:**  
Previous attempts to reduce spacing were reverted due to readability issues with numbers being too small or clipped. Need to find balance between compact spacing and readability.

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

### Bug #7: 🐛 Calendar Day Circles Too Small
**Status:** Open  
**Date Reported:** 2025-07-20  
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

**Additional Information:**  
The circles are currently sized at 32dp in the calendar_day_layout.xml. They should be increased for better usability.

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

### Bug #11: 🐛 Add Activity Dialog Navigation Issue from Dashboard/Calendar
**Status:** Open  
**Date Reported:** 2025-07-20  
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

**Additional Information:**  
- The Save button does successfully save the activity data
- Navigation works correctly when using add activity from Settings
- Issue is specific to Dashboard and Calendar entry points

---

### Bug #12: 🐛 Missing Delete Activity Feature in Timeline Tab
**Status:** Open  
**Date Reported:** 2025-07-20  
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

**Additional Information:**  
- Delete action should remove the activity from the database permanently
- Consider adding confirmation dialog to prevent accidental deletions
- Should update the timeline view immediately after deletion

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