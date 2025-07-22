# Known Issues

This document tracks known issues, limitations, and planned improvements for the PlayStreak 🎵 app.

## Status Legend
- 🔍 **Investigating** - Issue is being investigated or reproduced
- 🔄 **In Progress** - Fix is currently being developed
- 📋 **Planned** - Issue is acknowledged and planned for future release
- ⏳ **Coming Soon** - Feature/fix is actively being developed and will be available soon
- 🐛 **Bug** - Confirmed bug that needs fixing
- 💡 **Enhancement** - Improvement or new feature request

## Current Known Issues

### Issue #1: 🔄 Google Drive Functionality
**Status:** Coming Soon  
**Category:** Enhancement  
**Description:** Google Drive integration for backup and sync functionality is currently under development and will be available in a future release.  
**Impact:** Users cannot currently backup their practice data to Google Drive or sync across devices.  
**Workaround:** Use CSV export/import functionality for manual data backup and transfer.

### Issue #2: 🐛 Calendar Month Navigation Issue
**Status:** Investigating  
**Category:** Bug  
**Description:** In some cases, users can accidentally swipe and change the calendar month when interacting with calendar components, which may cause unexpected navigation or date selection issues.  
**Impact:** Users may inadvertently navigate to different months when trying to interact with calendar elements.  
**Workaround:** Be careful when swiping near calendar components. If month changes unexpectedly, navigate back using calendar controls.

### Issue #3: 📋 Import/Export Pieces
**Status:** Planned  
**Category:** Enhancement  
**Description:** Currently, the import/export functionality only handles activities. There is no way to import or export pieces/techniques separately, which limits data portability and makes it difficult to share repertoire lists or backup piece collections independently.  
**Impact:** Users cannot backup or transfer their piece/technique lists separately from activities, and cannot easily share repertoire with other users.  
**Workaround:** Use CSV export/import which includes piece names within activity data, though this requires having activities recorded for each piece.

### Issue #4: 📋 Import/Export Favorites
**Status:** Planned  
**Category:** Enhancement  
**Description:** While CSV import/export preserves favorite status of pieces that have recorded activities, there's no dedicated way to backup and restore favorite piece selections. This is particularly problematic when users want to maintain their favorite pieces across app reinstalls or device transfers.  
**Impact:** Users may lose their carefully curated favorite piece selections during data transfers or when setting up the app on a new device.  
**Workaround:** Favorite status is preserved through CSV export/import for pieces that have recorded activities, but pieces without activities will lose favorite status.

### Issue #5: 📋 Configuration in Settings
**Status:** Planned  
**Category:** Enhancement  
**Description:** The app currently lacks comprehensive user configuration options in the Settings screen. Users cannot customize app behavior, preferences, or default values for various features, limiting personalization and workflow optimization.  
**Impact:** Users cannot tailor the app experience to their specific practice routines, preferences, or workflow needs, potentially reducing efficiency and satisfaction.  
**Workaround:** Users must work within default app behaviors and cannot customize settings to match their individual preferences.

---

## Resolved Issues

*No resolved issues to display yet. Issues will be moved here once fixed.*

---

## How to Report Issues

If you encounter a bug or have a feature request:

1. **Check this document** to see if the issue is already known
2. **Check the [features.md](./features.md)** document for requested features
3. **Gather information** about the issue:
   - Steps to reproduce
   - Expected vs. actual behavior
   - Device/Android version information
   - Screenshots if applicable
4. **Report through appropriate channels** (GitHub issues, feedback, etc.)

---

*Last updated: 2025-07-22*