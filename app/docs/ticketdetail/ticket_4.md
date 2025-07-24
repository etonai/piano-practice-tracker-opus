# Ticket #4: Activity Limit Not Enforced on Manual Add

## Overview
Currently, the activity limit (3000 activities) is only enforced during CSV import. Users can manually add new activities even after exceeding the activity limit, which defeats the purpose of the limit and can lead to performance issues or data inconsistency.

## Current Behavior
- When importing activities via CSV, the app checks the activity limit and prevents import if the limit would be exceeded.
- When manually adding a new activity (via Add Activity or Quick Add), the app does **not** check the activity limit. Users can continue adding activities beyond the intended maximum.

## Expected Behavior
- The app should prevent users from adding a new activity (via any method) if they have already reached the activity limit (3000 activities).
- Users should see a clear error message if they attempt to add an activity beyond the limit.

## Impact
- Users can exceed the intended activity cap, which may cause performance degradation, unexpected behavior, or data management issues.
- The activity limit is not consistently enforced, leading to confusion and potential support issues.

## Steps to Reproduce
1. Add activities until you reach the activity limit (3000).
2. Continue adding activities manually (via Add Activity or Quick Add).
3. Observe that the app allows you to add more than 3000 activities.
4. Try importing activities via CSV; the app will block import if the limit is exceeded.

## Acceptance Criteria
- [ ] The app checks the activity limit before allowing manual addition of a new activity.
- [ ] If the user is at or above the activity limit, show a clear error message and prevent the addition.
- [ ] The activity limit is enforced consistently across all entry points (manual add, quick add, import).
- [ ] Add tests to verify the limit is enforced.

## Priority
**High** - This is a core data integrity and user experience issue. 