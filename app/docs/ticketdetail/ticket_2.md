# Ticket #2: Bug - Practice Suggestions Auto-Add 30 Minutes Duration

## Overview
When adding a practice activity from the practice suggestions in the Suggestions tab, the system automatically sets a duration of 30 minutes instead of leaving the duration field empty for user input.

## Bug Details

### Current Behavior
- User taps "+" button on a practice suggestion in the Suggestions tab
- System automatically creates a practice activity with 30 minutes duration
- User cannot easily modify or remove this pre-filled duration

### Expected Behavior
- User taps "+" button on a practice suggestion in the Suggestions tab
- System creates a practice activity with no duration pre-filled
- User must manually enter the duration they want

### Impact
- **User Experience:** Confusing and potentially incorrect duration values
- **Data Quality:** May lead to inaccurate practice tracking
- **Workflow:** Forces users to manually correct duration after quick-add

## Technical Investigation Needed

### Files to Examine
- `SuggestionsFragment.kt` - Quick-add button implementation
- `QuickAddActivityDialogFragment.kt` - Dialog for quick activity addition
- `QuickAddActivityViewModel.kt` - ViewModel handling quick-add logic
- Any related layout files for the quick-add dialog

### Potential Root Cause
- Default duration value being set in quick-add logic
- Missing duration field clearing in quick-add workflow
- Incorrect initialization of duration field

## Acceptance Criteria
- [ ] Quick-add from suggestions creates practice with empty duration field
- [ ] User must manually enter duration for quick-added practices
- [ ] No default duration values are pre-filled
- [ ] Existing manual add activity flow remains unchanged

## Implementation Notes
- This is likely a simple fix in the quick-add logic
- Should not affect the regular "Add Activity" flow
- May need to update UI to make duration field required for quick-add

## Priority
**High** - This affects core functionality and user experience

## Estimated Effort
**Low** - Likely a small code change in quick-add logic

## Related Components
- Suggestions tab
- Quick-add functionality
- Practice activity creation
- Duration field handling 