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
Attempted fix by adding `android:nestedScrollingEnabled="false"` to CalendarView in XML layout. Need to verify if this resolves the issue.

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

### Bug #3: 🐛 Pieces Sort Line Too Long - Needs Text Optimization
**Status:** Open  
**Date Reported:** 2025-07-20  
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

**Additional Information:**  
Text changes needed:
- "activities" → "activity"  
- "Last Date" → "Date"

---

### Bug #4: 🐛 Missing Buffer Space at Bottom of Tabs
**Status:** Open  
**Date Reported:** 2025-07-20  
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

**Additional Information:**  
Buffer space should be added to:
- Home tab
- Calendar tab  
- Pieces tab

Recommended buffer height: 80-100dp to accommodate floating action buttons and navigation elements.

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