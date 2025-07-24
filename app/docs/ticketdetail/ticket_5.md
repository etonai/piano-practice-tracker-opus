# Ticket #5: Delete Piece with Trash Icon in Pieces Tab

## Overview
Currently, users cannot delete pieces from the Pieces tab. To improve data management and user control, add a trash icon for each piece in the Pieces tab. When a user deletes a piece, all activities associated with that piece should also be deleted.

## User Story
As a user, I want to be able to delete pieces I no longer need, and have all related activities removed as well, so that my data stays organized and relevant.

## Current Behavior
- There is no way to delete a piece from the Pieces tab.
- Pieces and their associated activities remain in the database unless removed manually (not possible via UI).

## Expected Behavior
- Each piece in the Pieces tab has a trash icon.
- Tapping the trash icon prompts the user to confirm deletion.
- Confirming deletes the piece and all activities linked to it.
- The UI updates to reflect the removal.

## Acceptance Criteria
- [ ] Add a trash icon to each piece in the Pieces tab.
- [ ] Tapping the icon prompts for confirmation ("Delete this piece and all its activities?").
- [ ] On confirmation, delete the piece and all associated activities from the database.
- [ ] The Pieces tab and Timeline update to reflect the deletion.
- [ ] Add tests to verify correct deletion and data integrity.

## Technical Notes
- Deleting a piece should cascade to delete all activities with that piece's ID (foreign key constraint or manual deletion).
- Ensure UI updates are smooth and user receives feedback.
- Consider undo functionality (optional, not required for MVP).

## Priority
**Medium** - Improves user control and data management, but not release-blocking. 