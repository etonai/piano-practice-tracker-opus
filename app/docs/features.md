# Feature Tracking

This document tracks feature requests, their status, and implementation details for the PlayStreak 🎵 app.

## Status Legend
- 💡 **Requested** - Feature has been suggested and is under consideration
- 🔄 **In Progress** - Feature is currently being developed
- 🔍 **Verifying** - Feature implementation is complete and undergoing testing/verification
- ✅ **Implemented** - Feature has been completed, verified, and is available
- ❌ **Declined** - Feature request declined (e.g., not aligned with app goals, technical limitations)
- 🔮 **Future** - Feature planned for future release

## Feature Requests

### Feature #1: ✅ Pro Mode - Premium Feature Differentiation
**Status:** Implemented  
**Date Requested:** 2025-07-21  
**Date Implemented:** 2025-07-21  
**Priority:** High  
**Requested By:** Business Development  

**Description:**  
Implement a Pro Mode system that differentiates between Free and Pro users, with Pro users having access to enhanced features and capabilities. This will include a global setting mechanism to track user subscription status and conditionally enable premium features throughout the app.

**User Story:**  
As a business, we want to offer premium features to Pro subscribers so that we can generate revenue while still providing value to free users, and as a Pro user, I want access to enhanced features that justify my subscription cost.

**Acceptance Criteria:**  
- [x] Global `isProUser` setting/preference system
- [x] Pro user status detection and persistence across app sessions
- [ ] UI indicators showing Pro-only features (with upgrade prompts for free users)
- [ ] Graceful feature limiting for free users without breaking core functionality
- [ ] Pro user onboarding and feature discovery
- [x] Settings screen showing current subscription status
- [x] Feature gate system that can be easily extended for new Pro features
- [ ] Upgrade prompt system for free users attempting to access Pro features

**Technical Considerations:**  
- Implement global user preference system (`SharedPreferences` or Room database)
- Create `ProUserManager` or similar class to handle Pro status checks
- Design feature gate annotations or utility methods for easy Pro feature implementation
- Update UI components to show Pro badges/indicators where appropriate
- Consider future integration with Google Play Billing for actual subscription management
- Ensure backwards compatibility and graceful degradation for free users
- Create consistent upgrade prompt UI patterns

**Priority Justification:**  
High priority as this establishes the foundation for monetization strategy and must be implemented before rolling out premium features. This system will enable future revenue generation while maintaining a good free user experience.

**Implementation Details:**
- **ProUserManager**: Created singleton class managing Pro status via SharedPreferences
- **Storage**: Uses `playstreak_pro_prefs` SharedPreferences with `is_pro_user` boolean key
- **API**: Provides `isProUser()`, `setProUser(boolean)`, `toggleProStatus()`, and `resetToFreeUser()` methods
- **Testing**: Added Pro status toggle button in Settings for development/testing purposes
- **Future Migration**: Architecture supports easy integration with Google Play Billing later

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/utils/ProUserManager.kt` (new)
- `app/src/main/java/com/pseddev/playstreak/ui/main/MainFragment.kt`
- `app/src/main/res/layout/fragment_main.xml`

**Implementation Notes:**  
- Start with simple boolean flag system that can be extended later
- Design UI patterns that clearly communicate value of Pro features
- Ensure all existing features remain available to free users initially
- Plan migration path for implementing actual subscription system later

---

### Feature #2: ✅ Pro Badge in Settings Title
**Status:** Implemented  
**Date Requested:** 2025-07-21  
**Date Implemented:** 2025-07-21  
**Priority:** High  
**Requested By:** Development Team  

**Description:**  
Display "Pro" after "PlayStreak" in the Settings page title when the user is a Pro subscriber. This serves as a simple visual indicator of Pro status and will be the first feature to test the Pro/Free user differentiation system.

**User Story:**  
As a Pro user, I want to see visual confirmation of my Pro status in the Settings page so that I know my subscription is recognized by the app.

**Acceptance Criteria:**  
- [x] Settings page title shows "PlayStreak" for free users
- [x] Settings page title shows "PlayStreak Pro" for Pro users
- [x] Title updates dynamically when Pro status changes
- [x] Consistent styling and formatting for both title variants

**Technical Considerations:**  
- Integrate with the global `isProUser` setting system from Feature #1
- Update Settings fragment to check Pro status and modify title accordingly
- Ensure title updates if Pro status changes during the session
- Consider using string resources for easy localization

**Priority Justification:**  
High priority as this is the first implementation to test the Pro Mode infrastructure. It's simple, low-risk, and provides immediate visual feedback to validate the Pro user detection system works correctly.

**Implementation Details:**
- **Title Logic**: Settings page title dynamically updates based on `ProUserManager.isProUser()`
- **Display**: Shows "PlayStreak" for free users, "PlayStreak Pro" for Pro users
- **Real-time Updates**: Title refreshes in `onResume()` to reflect Pro status changes
- **Testing Integration**: Works seamlessly with Pro status toggle button for testing

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/ui/main/MainFragment.kt`

**Implementation Notes:**  
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Serves as a proof-of-concept for the Pro user differentiation system
- Can be used to test Pro status toggling during development

---

### Feature #3: ✅ Limited Heat Map for Free Users
**Status:** Implemented  
**Date Requested:** 2025-07-21  
**Date Implemented:** 2025-07-21  
**Priority:** High  
**Requested By:** Product Team  

**Description:**  
Implement a limited heat map feature for the calendar that differentiates between Free and Pro users. Free users will see a simplified heat map where days with any activities show as light blue, while Pro users will see the full heat map with multiple color intensities based on activity levels.

**User Story:**  
As a Free user, I want to see which days I practiced (light blue) so that I can track my practice consistency, and as a Pro user, I want to see detailed activity intensity levels so that I can better understand my practice patterns and volume.

**Acceptance Criteria:**  
- [x] Free users see binary heat map: no color for days with no activities, light blue for days with any activities
- [x] Pro users see full heat map with multiple color intensities based on activity count/duration
- [x] Heat map updates immediately when Pro status changes
- [x] Consistent visual experience within each user tier
- [x] Clear visual differentiation between Free and Pro heat map experiences
- [x] Activity Level Color Guide hidden for free users, visible for pro users

**Technical Considerations:**  
- Integrate with existing calendar heat map logic
- Use ProUserManager to determine which heat map to display
- Modify color calculation logic based on user tier
- Ensure heat map updates when toggling between Pro/Free modes
- Consider performance implications of heat map calculations

**Priority Justification:**  
High priority as this provides a clear value proposition for Pro users while still giving Free users basic visual feedback about their practice consistency.

**Implementation Details:**
- **Free User Heat Map**: Shows light blue (`#B3D9FF`) for any day with activities, white for no activities
- **Pro User Heat Map**: Full heat map with intensity levels:
  - Practice days: Light blue → Medium blue → Dark blue (based on activity count)
  - Performance days: Light green → Medium green → Dark green (based on activity count)
- **Activity Level Color Guide**: 
  - Hidden (`View.GONE`) for free users - no need to show complex legend for simple heat map
  - Visible (`View.VISIBLE`) for Pro users - shows detailed legend for 6 different color/intensity levels
- **Pro Status Detection**: Uses `ProUserManager.isProUser()` to determine which heat map to display
- **Dynamic Updates**: Calendar and color guide refresh in `onResume()` when Pro status changes
- **Preserved Logic**: All existing Pro heat map logic maintained for Pro users

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/ui/progress/CalendarFragment.kt`
- `app/src/main/res/layout/fragment_calendar.xml`

**Implementation Notes:**  
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Provides tangible differentiation between Free and Pro user experiences
- Can serve as a model for other Pro/Free feature implementations

---

### Feature #4: ✅ Disable Import from CSV for Free Users
**Status:** Implemented  
**Date Requested:** 2025-07-21  
**Date Implemented:** 2025-07-21  
**Priority:** High  
**Requested By:** Product Team  

**Description:**  
Restrict CSV import functionality to Pro users only. Free users will see the Import/Export Data page but the import functionality will be disabled with an upgrade prompt. Export functionality will remain available to all users to ensure they can always access their data.

**User Story:**  
As a business, we want to limit advanced data management features like CSV import to Pro users so that we can provide additional value for Pro subscriptions, while as a Free user, I can still export my data but need to upgrade to Pro to import external data.

**Acceptance Criteria:**  
- [x] Free users can access Import/Export Data page
- [x] Free users can still export their data to CSV (data portability)
- [x] Free users cannot import CSV files - import functionality disabled
- [x] Import button/section shows upgrade prompt for Free users
- [x] Pro users retain full import and export functionality
- [x] Clear messaging about Pro-only import feature
- [x] Import functionality updates immediately when Pro status changes

**Technical Considerations:**  
- Integrate with ProUserManager to check user status
- Disable/enable import UI elements based on Pro status
- Add upgrade prompt UI for import functionality
- Maintain export functionality for all users (data portability)
- Update import screens to check Pro status before allowing import
- Handle Pro status changes during import workflow

**Priority Justification:**  
High priority as this provides a valuable Pro feature that encourages upgrades while maintaining ethical data portability (users can always export their data).

**Implementation Details:**
- **Import Button State**: 
  - Free users: Button text shows "Import from CSV (Pro Only)" with 0.6 alpha transparency
  - Pro users: Button text shows "Import from CSV" with full opacity
- **Import Functionality**:
  - Free users: Clicking import button shows upgrade prompt dialog instead of file picker
  - Pro users: Full import functionality as before
- **Export Functionality**: Remains unchanged and available to all users (data portability)
- **Upgrade Prompt**: Professional dialog explaining Pro feature with "Learn More" button
- **Dynamic Updates**: Button state refreshes in `onResume()` when Pro status changes
- **Pro Status Detection**: Uses `ProUserManager.isProUser()` throughout import workflow

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/ui/importexport/ImportExportFragment.kt`

**Implementation Notes:**  
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Maintains user data portability by keeping export functionality free
- Creates clear incentive for Pro upgrade with advanced data management features

---

### Feature #5: ✅ Limit Free Users to 4 Favorites
**Status:** Implemented  
**Date Requested:** 2025-07-21  
**Date Implemented:** 2025-07-22  
**Priority:** High  
**Requested By:** Product Team  

**Description:**  
Implement a simple restriction where Free users cannot add new favorites if they already have 4 or more favorites. Pro users have unlimited favorites. The restriction only applies to adding new favorites - removing favorites is always allowed for all users.

**User Story:**  
As a business, we want to provide a simple incentive for Pro upgrades by limiting Free users to 4 favorites, while as a Pro user, I want unlimited favorites to organize my extensive repertoire without restrictions.

**Acceptance Criteria:**  
- [x] Free users cannot add new favorites if they already have 4 or more favorites
- [x] Pro users have unlimited favorites (no restrictions)
- [x] When Free user with 4+ favorites tries to add another, show upgrade prompt instead of adding
- [x] Removing favorites is always allowed for all users regardless of Pro status
- [x] Existing Free users with >4 favorites can keep them all and remove any, but cannot add new ones
- [x] Clear messaging in upgrade prompt about Pro unlimited favorites
- [x] Simple favorite count check before allowing new additions
- [x] No changes to existing toggle behavior for removing favorites

**Technical Considerations:**  
- Simple count check before allowing new favorite additions
- Minimal impact on existing data flow and suggestion systems
- Only affects the "add favorite" action, not remove
- Use straightforward favorite count from repository data
- Avoid complex UI state management or return value changes to toggle methods

**Priority Justification:**  
High priority as this provides a clear but non-intrusive Pro upgrade incentive. The simplified approach minimizes technical complexity while still providing business value.

**Implementation Details:**
- **Favorite Limit Logic**: Added `canAddMoreFavorites()` method to ProUserManager with FREE_USER_FAVORITE_LIMIT constant (4)
- **Pro User Behavior**: Unlimited favorites, no restrictions or changes to existing workflow
- **Free User Logic**: 
  - Can always remove favorites (no restrictions on unfavoriting)
  - Can add favorites only if current count is < 4
  - If attempting to add when already at 4+, show upgrade prompt instead
- **Upgrade Prompt**: Professional dialog explaining limit with "Learn More" button for future Pro upgrade flow
- **Return Values**: toggleFavorite() methods return boolean (true = success, false = limit reached)
- **UI Feedback**: Snackbar confirmation for successful toggles, upgrade prompt for limit reached

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/utils/ProUserManager.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/progress/PiecesViewModel.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/progress/PiecesFragment.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/favorites/FavoritesViewModel.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/favorites/FavoritesFragment.kt`

**Implementation Notes:**  
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Simplified implementation avoids complex data synchronization issues
- Only affects adding favorites, not removing them
- Maintains all existing suggestion functionality

---

### Feature #6: ✅ Hide Inactive Tab for Free Users
**Status:** Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** High  
**Requested By:** Product Team  

**Description:**  
Hide the "Inactive" tab from Free users while keeping it visible for Pro users. This provides another Pro-only feature that encourages upgrades while simplifying the interface for Free users.

**User Story:**  
As a business, we want to limit advanced features like the Inactive tab to Pro users so that we can provide additional value for Pro subscriptions, while as a Free user, I have a simpler interface focused on core functionality.

**Acceptance Criteria:**  
- [x] Free users cannot see the "Inactive" tab
- [x] Pro users can see and use the "Inactive" tab normally
- [x] Tab visibility updates immediately when Pro status changes
- [x] No crashes or UI issues when tab is hidden/shown
- [x] Free users still have access to all other tabs and core functionality
- [x] Clear Pro-only feature differentiation

**Technical Considerations:**  
- Identify where the Inactive tab is implemented
- Use ProUserManager to determine tab visibility
- Handle dynamic tab showing/hiding based on Pro status changes
- Ensure proper UI layout when tab is hidden
- Maintain existing functionality for Pro users

**Priority Justification:**  
High priority as this provides a clear Pro feature differentiation that encourages upgrades while keeping the core app functionality available to Free users.

**Implementation Details:**
- **Tab Visibility Logic**: Inactive tab only shown when `proUserManager.isProUser()` returns true
- **Adapter Count**: ViewPager adapter returns 6 items for Pro users, 5 items for Free users
- **Dynamic Updates**: Tab setup refreshed in `onResume()` to handle Pro status changes
- **Free User Experience**: 
  - Shows 5 tabs: Dashboard, Calendar, Suggestions, Pieces, Timeline
  - No access to Inactive tab or AbandonedFragment
- **Pro User Experience**: 
  - Shows all 6 tabs including Inactive tab
  - Full access to all functionality unchanged
- **Error Handling**: Proper position validation prevents crashes when tab is hidden

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/ui/progress/ViewProgressFragment.kt`

**Implementation Notes:**  
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Simple implementation that cleanly hides the tab without affecting other functionality
- Tab refreshes automatically when Pro status changes during app usage

---

### Feature #7: ✅ Limit Suggestions for Free Users
**Status:** Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** High  
**Requested By:** Product Team  

**Description:**  
Limit Free users to only 2 favorite suggestions and 2 non-favorite suggestions in both the Dashboard and Suggestions tab. Pro users continue to see the full suggestion lists (up to 4 favorites and 4 non-favorites each).

**User Story:**  
As a business, we want to limit the number of practice suggestions for Free users so that we can provide additional value for Pro subscriptions, while as a Pro user, I want access to comprehensive practice suggestions to optimize my practice routine.

**Acceptance Criteria:**  
- [x] Free users see maximum 1 favorite suggestion (updated from 2 by Feature #8)
- [x] Free users see maximum 2 non-favorite suggestions (instead of up to 4)
- [x] Pro users continue to see full suggestion lists (up to 4 each)
- [x] Limitation applies to both Dashboard and Suggestions tab
- [x] Suggestion quality maintained - show the most relevant suggestions within limits
- [x] Suggestion limits update immediately when Pro status changes
- [x] No crashes or UI issues when suggestions are limited

**Technical Considerations:**  
- Modify DashboardViewModel suggestions logic to limit results for Free users
- Modify SuggestionsViewModel to apply same limits for Free users
- Use ProUserManager to determine suggestion limits
- Maintain existing suggestion ranking/priority algorithms
- Apply limits after suggestions are calculated but before returning to UI
- Ensure consistent behavior across both Dashboard and Suggestions tab

**Priority Justification:**  
High priority as this provides valuable Pro differentiation while maintaining core suggestion functionality for Free users. Practice suggestions are a key feature that serious practitioners will want unlimited access to.

**Implementation Details:**
- **Suggestion Limits**: 
  - Free users: Maximum 1 favorite suggestion + 2 non-favorite suggestions (favorite limit updated by Feature #8)
  - Pro users: Up to 4 favorite suggestions + 4 non-favorite suggestions (unchanged)
- **Dynamic Limits**: Both ViewModels check `proUserManager.isProUser()` to determine limits
- **Consistent Implementation**: Same logic applied to both DashboardViewModel and SuggestionsViewModel
- **Suggestion Quality**: Existing ranking and fallback algorithms preserved within the reduced limits
- **Real-time Updates**: Limits apply immediately when Pro status changes since ViewModels react to Pro status
- **UI Impact**: Dashboard shows fewer suggestions for Free users (3 total vs up to 8), Suggestions tab shows fewer items

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/progress/DashboardFragment.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/progress/SuggestionsViewModel.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/progress/SuggestionsFragment.kt`

**Implementation Notes:**  
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Maintains all existing suggestion algorithm logic and quality
- Provides clear value proposition for Pro upgrade
- No UI changes needed - suggestion limits work seamlessly with existing displays
- **Status Update**: Original specification called for 2 favorite suggestions, but Feature #8 further reduced this to 1 favorite suggestion. The non-favorite limit of 2 remains as originally specified.

---

### Feature #8: ✅ Single Favorite Suggestion for Free Users
**Status:** Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** High  
**Requested By:** Product Team  

**Description:**  
Limit Free users to only 1 favorite suggestion (instead of 2) in both the Dashboard and Suggestions tab. Free users will always see their least recently practiced/performed favorite piece as the single suggestion. If they have no favorite pieces, no favorite suggestions are shown. Pro users continue to see up to 4 favorite suggestions.

**User Story:**  
As a business, we want to further limit favorite suggestions for Free users to provide even stronger Pro upgrade incentive, while as a Free user, I still get the most important favorite suggestion (my most neglected favorite piece) to maintain practice value.

**Acceptance Criteria:**  
- [x] Free users see exactly 1 favorite suggestion (instead of 2)
- [x] Free users with no favorites see 0 favorite suggestions  
- [x] The single favorite suggestion is always the least recently practiced/performed favorite piece
- [x] Pro users continue to see up to 4 favorite suggestions (unchanged)
- [x] Limitation applies to both Dashboard and Suggestions tab
- [x] Non-favorite suggestions remain at 2 for Free users
- [x] Suggestion limits update immediately when Pro status changes

**Technical Considerations:**  
- Modify DashboardViewModel and SuggestionsViewModel favorite limits from 2 to 1 for Free users
- Maintain existing "least recently practiced" algorithm for the single suggestion
- Ensure proper handling when user has no favorites (empty list)
- Keep non-favorite suggestion limits unchanged (2 for Free, 4 for Pro)

**Priority Justification:**  
High priority as this creates a stronger Pro upgrade incentive while still providing valuable practice guidance to Free users with their most neglected favorite piece.

**Implementation Details:**
- **Favorite Limit Change**: Updated from 2 to 1 favorite suggestions for Free users in both ViewModels
- **Algorithm Preserved**: Existing "least recently practiced/performed" selection logic maintained
- **Pro Users Unchanged**: Continue to see up to 4 favorite suggestions
- **Non-favorite Limits**: Unchanged at 2 for Free users, 4 for Pro users
- **Empty Handling**: When Free users have no favorites, suggestion list naturally shows 0 favorite suggestions
- **Total Suggestions**: Free users now see maximum 3 suggestions total (1 favorite + 2 non-favorite)

**Files Modified:**
- `app/src/main/java/com/pseddev/playstreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/playstreak/ui/progress/SuggestionsViewModel.kt`

**Implementation Notes:**  
- Simple change from `4 else 2` to `4 else 1` in favorite limit logic
- Leverages existing suggestion algorithms - no complex changes needed
- Provides stronger Pro upgrade incentive while maintaining core value for Free users
- Works seamlessly with existing UI - no interface changes required

---

### Feature #9: 🔄 Remove Pro Upgrade Prompts for Free-Only Release
**Status:** ✅ Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** High  
**Requested By:** Product Team  

**Description:**  
Remove all Pro upgrade prompts and monetization messaging for the initial Free-only release of PlayStreak. Users will experience Free tier limitations without upgrade pressure, creating a clean user experience focused on core functionality.

**User Story:**  
As a user downloading the Free version of PlayStreak, I want to understand the app's limitations without being pressured to upgrade, so that I can focus on using the core features and providing authentic feedback about the app's value.

**Acceptance Criteria:**  
- [ ] Import CSV prompt changed from Pro upgrade to "Coming Soon" message
- [ ] Favorites limit prompt changed from Pro upgrade to simple limitation explanation
- [ ] No "Learn More" or "Upgrade" buttons in limitation messages
- [ ] All upgrade-related messaging removed from the app
- [ ] Users still experience authentic Free tier limitations (4 favorites, 1 favorite suggestion, etc.)
- [ ] Clean, professional messaging about current app capabilities
- [ ] Pro/Free logic remains intact for future Pro launch

**Technical Considerations:**  
- Modify ImportExportFragment upgrade prompt to show "Coming Soon" message
- Update favorites limit prompts in PiecesFragment and FavoritesFragment
- Remove upgrade action buttons but keep informative dialogs
- Preserve underlying Pro/Free differentiation logic for future use
- Maintain current limitation behaviors without monetization pressure

**Priority Justification:**  
High priority for initial app release strategy. Allows focus on core app quality and user feedback without monetization complexity, while preserving architecture for future Pro launch.

**Implementation Approach:**
- **Import CSV**: Change "Pro Feature" dialog to "Coming Soon - Import from CSV functionality will be available in a future update"
- **Favorites Limit**: Change "Pro Feature" dialog to "Favorites Limit - This app supports up to 4 favorite pieces to help you focus on your most important repertoire"
- **Preserve Logic**: Keep all existing Pro/Free differentiation code intact for easy future conversion back to upgrade prompts

---

### Feature #10: 💡 Google Drive Functionality Coming Soon Message
**Status:** ✅ Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** Medium  
**Requested By:** Product Team  

**Description:**  
Add "Google Drive functionality coming soon" messaging to the Import/Export Data page to inform users about planned cloud storage integration for data backup and sync capabilities.

**User Story:**  
As a user of PlayStreak, I want to know that Google Drive integration is planned so that I can look forward to cloud backup and sync capabilities for my practice data in future updates.

**Acceptance Criteria:**  
- [ ] Import/Export Data page shows "Google Drive functionality coming soon" message
- [ ] Message is clearly visible and professionally presented
- [ ] Message doesn't interfere with existing export functionality
- [ ] Gives users expectation of future cloud storage features
- [ ] Consistent with overall app messaging style

**Technical Considerations:**  
- Add informational message/section to ImportExportFragment
- Place message prominently but not intrusively on the Import/Export screen
- Consider adding icon or visual element to make message more noticeable
- Ensure message fits well with existing UI layout

**Priority Justification:**  
Medium priority feature that helps set user expectations about upcoming cloud functionality while building anticipation for future updates. Supports user retention by showing ongoing development.

**Implementation Approach:**
- Add a dedicated "Coming Soon" section or card to the Import/Export screen
- Include Google Drive icon if available
- Use encouraging messaging like "Google Drive sync and backup functionality coming in a future update!"
- Position prominently but maintain focus on current export capabilities

---

### Feature #11: 💡 Purge All Data Button Testing Mode
**Status:** ✅ Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** Medium  
**Requested By:** Development Team  

**Description:**  
Add "(Testing)" label to the Purge All Data button and implement functionality to hide/show this button for production vs testing builds using BuildConfig.DEBUG. The button should be visible during development and testing but hidden in production releases, with ability to reveal it when needed for testing purposes.

**User Story:**  
As a developer, I want to be able to easily reset all app data during testing while ensuring production users don't accidentally access this destructive functionality, and as a tester, I want to be able to reveal the purge button when needed for test scenarios.

**Acceptance Criteria:**  
- [ ] Purge All Data button shows "(Testing)" label in the text
- [ ] Button is hidden by default in production builds
- [ ] Button can be revealed through a testing mechanism (debug menu, build flag, etc.)
- [ ] Button remains fully functional when visible
- [ ] Clear visual indication that this is a testing/development feature
- [ ] No accidental access by regular users in production
- [ ] Easy to enable for QA testing scenarios

**Technical Considerations:**  
- Add "(Testing)" text to the button label
- Implement build variant or debug flag to control button visibility
- Consider adding to existing debug/testing menu if available
- Ensure button functionality remains unchanged when visible
- Use BuildConfig.DEBUG or similar mechanism for production detection
- Alternative reveal methods: long press, secret gesture, or settings toggle

**Priority Justification:**  
Medium priority development tool that improves testing workflow while protecting production users from accidental data loss. Important for QA processes and development efficiency.

**Implementation Approach:**
- **Button Label**: Change to "Purge All Data (Testing)" 
- **Visibility Control**: Hide in production builds using `if (BuildConfig.DEBUG)` or similar
- **Testing Access**: Provide mechanism to reveal button when needed (debug menu, long press, etc.)
- **Safety**: Maintain existing confirmation dialogs when button is used
- **Documentation**: Clear instructions for testers on how to access the button

---

### Feature #12: 💡 Remove MIT License
**Status:** ✅ Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** High  
**Requested By:** Legal/Business Team  

**Description:**  
Remove the MIT License from the PlayStreak app and replace it with a commercial "All Rights Reserved" license to prepare for commercial release. This includes removing MIT license files, replacing with commercial copyright, and updating any license references in the app or documentation.

**User Story:**  
As a business preparing for commercial app release, I want to replace the open-source MIT License with a commercial "All Rights Reserved" license so that the app can be properly licensed as a commercial product with full ownership protection.

**Acceptance Criteria:**  
- [ ] Remove MIT LICENSE file from project root
- [ ] Add new COPYRIGHT file with "All Rights Reserved" commercial license
- [ ] Remove MIT license headers from all source files
- [ ] Add copyright notice to app About/Settings screen
- [ ] Update documentation files to reference commercial licensing
- [ ] Ensure no remaining MIT License artifacts in the codebase
- [ ] Update any build scripts or configuration that reference the license
- [ ] Professional commercial licensing presentation

**Technical Considerations:**  
- Search entire codebase for "MIT", "License", or license headers
- Remove or update LICENSE files in project root
- Check app's About/Settings screens for license display
- Review build.gradle files for license-related configurations
- Ensure third-party library licenses are handled separately
- Update README or other documentation that references licensing

**Priority Justification:**  
High priority for commercial release preparation. MIT License removal is essential before app store publication to avoid legal complications and establish proper commercial licensing.

**Implementation Approach:**
- **License Replacement**: Delete MIT LICENSE file and add COPYRIGHT file with commercial license
- **Commercial License Text**: "Copyright (c) 2025 [Your Name/Company]. All rights reserved. This software and associated documentation files are proprietary and confidential. Unauthorized copying, distribution, or modification is strictly prohibited."
- **Source Headers**: Remove MIT headers from .kt, .java files (optional commercial headers)
- **UI Integration**: Add copyright notice to About/Settings screen
- **Documentation**: Update README and other docs to reference commercial licensing
- **Build Configuration**: Update any license-related build configurations

---

### Feature #13: 💡 Hide Switch to Pro/Free Button Using BuildConfig.DEBUG
**Status:** 🔍 Verifying  
**Date Requested:** 2025-07-22  
**Priority:** High  
**Requested By:** Development Team  

**Description:**  
Hide the Switch to Pro/Free button in production builds using BuildConfig.DEBUG while keeping it visible in debug builds for testing. This ensures production users don't see testing functionality while maintaining developer/tester access to Pro status switching.

**User Story:**  
As a production user, I should not see testing buttons that allow switching between Pro and Free modes, while as a developer or tester, I want access to the Pro/Free switch button in debug builds to test different user experiences.

**Acceptance Criteria:**  
- [ ] Switch to Pro/Free button hidden in production/release builds
- [ ] Switch to Pro/Free button visible in debug builds
- [ ] Uses BuildConfig.DEBUG to control visibility
- [ ] Button functionality unchanged when visible
- [ ] No impact on other Pro/Free differentiation logic
- [ ] Clean production UI without testing artifacts
- [ ] Easy testing access in debug builds

**Technical Considerations:**  
- Locate Switch to Pro/Free button in Settings or main interface
- Wrap button visibility with `if (BuildConfig.DEBUG)` condition
- Ensure button container/layout adjusts properly when hidden
- Maintain existing button functionality when visible
- Consider using `View.GONE` vs `View.INVISIBLE` for clean layout
- Test both debug and release build variants

**Priority Justification:**  
High priority for production release cleanliness. Testing buttons should not be visible to production users as they create confusion and expose internal functionality not intended for end users.

**Implementation Approach:**
- **Visibility Control**: Use `button.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE`
- **Location**: Find Switch to Pro/Free button in Settings screen or similar
- **Layout**: Ensure parent layout handles hidden button gracefully
- **Testing**: Verify button works in debug builds and is hidden in release builds
- **Clean UI**: Production users see polished interface without testing elements

---

### Feature #14: 💡 Build Variant Default Pro Status
**Status:** ✅ Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** Critical  
**Requested By:** Business Team  

**Description:**  
Set different default Pro status based on build variant: Free mode default for release builds (production users), Pro mode default for debug builds (developers/testers). This ensures production users get the intended Free experience while developers can easily test Pro features.

**User Story:**  
As a production user installing PlayStreak, I should automatically start with the Free experience, while as a developer or tester using debug builds, I should start with Pro access to easily test all features without manual switching.

**Acceptance Criteria:**  
- [ ] Release builds: New installations default to Free mode (isProUser = false)
- [ ] Debug builds: New installations default to Pro mode (isProUser = true)
- [ ] ProUserManager initializes with different defaults based on BuildConfig.DEBUG
- [ ] Release builds: All Free tier limitations active from first app launch
- [ ] Debug builds: All Pro features accessible from first app launch
- [ ] Consistent experience within each build variant
- [ ] Fresh installs show appropriate UI based on build variant (release=5 tabs, debug=6 tabs)
- [ ] Existing users maintain their current Pro/Free status regardless of build variant

**Technical Considerations:**  
- Update ProUserManager initialization to use BuildConfig.DEBUG for default value
- Modify SharedPreferences default behavior to return build-variant-specific defaults
- Verify that all Pro/Free checks handle both debug and release defaults correctly
- Test fresh installation on both debug and release builds
- Ensure existing user data is not affected by changes (only impacts new installs)
- Consider that debug users may need to manually switch to Free for testing

**Priority Justification:**  
Critical priority for both production release and development efficiency. Production users must start with Free experience for proper evaluation and monetization, while developers need immediate Pro access for efficient feature testing.

**Implementation Approach:**  
- **Default Logic**: Use `BuildConfig.DEBUG ? true : false` as default Pro status for new users
- **Location**: Update ProUserManager.isProUser() method to return build-variant-specific defaults
- **Existing Users**: Preserve current Pro/Free status in SharedPreferences
- **Testing**: Verify debug builds start Pro, release builds start Free
- **ProUserManager Default**: Ensure `isProUser()` returns `false` when no preference exists
- **SharedPreferences**: Use `getBoolean(KEY_IS_PRO_USER, false)` with explicit false default
- **Fresh Install Testing**: Verify new installations show Free tier behavior
- **User Migration**: Ensure existing users (if any) maintain current status
- **Consistency Check**: All Pro/Free logic should assume Free by default

---

### Feature #15: 💡 Add "Add Piece (#)" Button to Settings Page
**Status:** ✅ Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** Medium  
**Requested By:** UI/UX Team  

**Description:**  
Add an "Add Piece" button to the Settings page positioned right after the "Add Activity" button. The button should display the current count of pieces in parentheses (e.g., "Add Piece (12)") to provide users with quick access to piece management and visibility into their repertoire size.

**User Story:**  
As a user, I want quick access to add new pieces from the Settings page and see at a glance how many pieces are in my repertoire, so that I can easily manage my practice library and understand the scope of my collection.

**Acceptance Criteria:**  
- [ ] "Add Piece" button added to Settings page after "Add Activity" button
- [ ] Button shows current piece count in parentheses format: "Add Piece (12)"
- [ ] Piece count updates dynamically when pieces are added/removed
- [ ] Button navigates to Add Piece screen/functionality
- [ ] Consistent styling with existing Settings page buttons
- [ ] Count reflects actual number of pieces in user's library
- [ ] Real-time count updates when returning to Settings page

**Technical Considerations:**  
- Add button to Settings page layout after Add Activity button
- Query repository to get current piece count
- Use LiveData or similar to observe piece count changes
- Navigate to appropriate Add Piece screen when clicked
- Ensure count updates when user returns from adding/removing pieces
- Consider performance impact of frequent count queries
- Handle zero pieces case gracefully ("Add Piece (0)")

**Priority Justification:**  
Medium priority UI enhancement that improves user experience by providing quick access to piece management and useful repertoire size information. Helps users understand and manage their practice library more effectively.

**Implementation Approach:**
- **Button Placement**: Add after existing Add Activity button in Settings layout
- **Count Query**: Use repository to get piece count with `repository.getAllPiecesAndTechniques().count { it.type == ItemType.PIECE }`
- **Dynamic Updates**: Observe piece data changes with LiveData
- **Navigation**: Navigate to Add Piece screen (create if doesn't exist)
- **Styling**: Match existing Settings page button design
- **Performance**: Cache count or use efficient query for real-time updates

---

### Feature #16: 💡 Set Application Limits for Activities and Pieces
**Status:** Requested  
**Date Requested:** 2025-07-22  
**Priority:** High  
**Requested By:** Engineering/QA Team  

**Description:**  
Investigate and establish reasonable limits for the number of activities that can be created per day and the total number of pieces the app can hold. Implement these limits with proper validation and user feedback to ensure stable app performance and prevent edge cases that could cause crashes or poor performance.

**User Story:**  
As a user, I want the app to function reliably even with extensive use, and as a developer, I want to ensure the app has tested, guaranteed performance limits rather than unknown breaking points that could cause crashes or poor user experience.

**Acceptance Criteria:**  
- [ ] Research and determine reasonable daily activity limits (e.g., 50-100 activities per day)
- [ ] Research and determine reasonable total piece limits (e.g., 500-1000 pieces)
- [ ] Test app performance at determined limits to ensure stability
- [ ] Implement validation to prevent exceeding limits
- [ ] Provide clear user feedback when approaching/hitting limits
- [ ] Graceful handling when limits are reached (no crashes)
- [ ] Document limits for user support, troubleshooting, and testing documentation
- [ ] Consider different limits for Free vs Pro users if applicable

**Technical Considerations:**  
- Performance testing with large datasets (database queries, UI rendering, memory usage)
- Database performance with thousands of activities and hundreds of pieces
- UI responsiveness with large lists (RecyclerView performance, pagination)
- Memory usage analysis under heavy data loads
- Export/import performance with maximum data sets
- Search and filtering performance with large datasets
- Calendar rendering with many activities per day

**Priority Justification:**  
High priority for app stability and user trust. Having undefined limits creates risk of crashes, poor performance, and bad user experience. Professional apps should have tested, documented limits to ensure reliability.

**Implementation Approach:**
- **Research Phase**: Performance testing with synthetic large datasets
- **Limit Determination**: Based on testing results and user research
- **Validation**: Add checks when creating activities/pieces
- **User Feedback**: Clear messages like "Daily activity limit reached (50/50)"
- **Graceful Degradation**: Disable add buttons/show alternative options when limits reached
- **Documentation**: Include limits in user guide, support materials, and testing documentation
- **Monitoring**: Track usage patterns to validate limit appropriateness

**Suggested Research Areas:**
- Database performance with 10,000+ activities and 1,000+ pieces
- Calendar view rendering with 50+ activities per day
- Suggestions algorithm performance with large datasets
- Export file size and generation time limits
- Memory usage patterns under maximum load

---

### Feature #17: 💡 Disable Import From CSV Button for Free Users
**Status:** ✅ Implemented  
**Date Requested:** 2025-07-22  
**Date Implemented:** 2025-07-22  
**Priority:** High  
**Requested By:** Business Team  

**Description:**  
Disable the Import From CSV button functionality for Free users while keeping it enabled for Pro users. This creates a clear Pro/Free differentiation for advanced data management features while maintaining export functionality for all users to ensure data portability.

**User Story:**  
As a Free user, I should not have access to CSV import functionality, while as a Pro user, I want full data import capabilities so that I can manage my practice data comprehensively.

**Acceptance Criteria:**  
- [ ] Free users: Import From CSV button disabled/grayed out
- [ ] Pro users: Import From CSV button fully functional
- [ ] Button visual state reflects availability (enabled/disabled styling)
- [ ] Clicking disabled button shows appropriate message (not "Coming Soon")
- [ ] Export functionality remains available to all users
- [ ] Pro/Free status changes update button state immediately
- [ ] Consistent with other Pro/Free feature differentiations

**Technical Considerations:**  
- Update ImportExportFragment to check Pro status for import button state
- Modify button click behavior based on Pro/Free status
- Ensure button styling reflects enabled/disabled state (alpha, colors)
- Replace "Coming Soon" message with Pro feature prompt for consistency
- Test button state changes when Pro status toggles
- Maintain export functionality for all users (data portability)

**Priority Justification:**  
High priority for establishing clear Pro/Free feature boundaries. Import functionality is an advanced data management feature that should be restricted to Pro users while ensuring all users can export their data.

**Implementation Approach:**
- **Button State**: Use `button.isEnabled = proUserManager.isProUser()` for import button
- **Visual Styling**: Apply appropriate alpha/styling for disabled state
- **Click Behavior**: Show Pro upgrade prompt when disabled button is clicked
- **Dynamic Updates**: Update button state when Pro status changes in onResume()
- **User Experience**: Clear visual distinction between enabled/disabled states

---

## Feature Request Template

When requesting new features, please use this template:

```markdown
### 💡 [Feature Title]
**Status:** Requested  
**Date Requested:** YYYY-MM-DD  
**Priority:** [Critical/High/Medium/Low/Future]  
**Requested By:** [Name/Community/User Feedback]

**Description:**  
[Clear description of the requested feature]

**User Story:**  
As a [user type], I want [functionality] so that [benefit/value].

**Acceptance Criteria:**  
- [ ] [Criterion 1]
- [ ] [Criterion 2]
- [ ] [Criterion 3]

**Technical Considerations:**  
[Any technical requirements, constraints, or implementation notes]

**Priority Justification:**  
[Why this feature has the assigned priority level]
```

---

## Implementation Guidelines

### Feature Development Process
1. **Analysis** - Evaluate feasibility, user value, and technical requirements
2. **Design** - Create UI/UX mockups and technical architecture
3. **Implementation** - Develop feature following coding standards
4. **Testing** - Unit tests, integration tests, and user acceptance testing
5. **Documentation** - Update user guides and developer documentation
6. **Release** - Deploy with appropriate version numbering

### Priority Levels
- **Critical** - Essential for app functionality or user safety
- **High** - Significant user value and feasible with current resources
- **Medium** - Good user value but lower priority than high items
- **Low** - Nice-to-have features that enhance experience
- **Future** - Aspirational features requiring significant planning or resources

### Technical Standards
- Follow existing code architecture and patterns
- Maintain consistency with current UI/UX design
- Ensure accessibility compliance
- Include comprehensive testing
- Document all new APIs and components