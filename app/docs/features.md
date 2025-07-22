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