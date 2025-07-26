# Ticket #7: Practice Duration Default Shows 15 But Doesn't Save

**Status:** 🐛 Open  
**Priority:** High  
**Date Created:** 2025-07-26  

## Description

When adding a practice session, the duration field displays "15" as placeholder text, giving users the impression that 15 minutes will be saved as the default. However, if the user doesn't manually edit this field and proceeds, the system saves an empty/null value instead of 15 minutes.

## Bug Details

### Current Behavior
1. User clicks to add practice activity
2. Duration field shows "15" as placeholder text
3. User assumes 15 minutes will be saved and clicks continue without editing
4. System saves empty/null duration instead of 15 minutes

### Expected Behavior
1. User clicks to add practice activity  
2. Duration field shows "5" as placeholder text
3. If user doesn't edit the field, system saves 5 minutes as the default
4. User can still edit to change the duration if desired

## Proposed Solution

1. **Change default value from 15 to 5 minutes**
2. **Ensure the default value is actually saved** when user doesn't edit the field

## Implementation Details

### Files to Investigate
- Duration input components in the add practice flow
- Form validation and saving logic
- Any related fragments or view models that handle practice duration

### Technical Requirements
- Update placeholder text from "15" to "5"
- Ensure form field has actual default value of 5, not just placeholder text
- Verify saving logic captures the default value when field is not edited
- Test that manual edits still work correctly

## Acceptance Criteria

- [ ] Duration field shows "5" as placeholder text when adding practice
- [ ] If user doesn't edit duration field, 5 minutes is saved to the database
- [ ] User can still manually edit the duration field to any valid value
- [ ] Saved practice activities show the correct duration (5 minutes when not edited)
- [ ] No regression in existing duration input functionality

## Impact

**Severity:** High - This affects user data accuracy and trust in the application

**User Experience:** Users expect the displayed value to be saved, so this creates confusion and incorrect data

**Data Integrity:** Practice sessions are being saved with missing duration data instead of meaningful defaults