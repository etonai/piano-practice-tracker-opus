# Practice Suggestions Algorithm Analysis

**File:** `SuggestionsService.kt`  
**Method:** `generatePracticeSuggestions(pieces: List<PieceOrTechnique>, activities: List<Activity>)`  
**Lines:** 172-379  
**Date:** 2025-08-01  

## Overview

The `generatePracticeSuggestions` method is responsible for generating practice suggestions for both favorite and non-favorite pieces. The algorithm uses time-based criteria to determine which pieces should be suggested for practice, with different rules for favorites vs non-favorites.

## Algorithm Flow

### 1. Initialization (Lines 176-177)
```kotlin
val favoriteSuggestions = mutableListOf<SuggestionItem>()
val nonFavoriteSuggestions = mutableListOf<SuggestionItem>()
```
Two separate lists are maintained for favorite and non-favorite suggestions.

### 2. Main Loop - Process Each Piece (Lines 179-238)

For each piece of type `ItemType.PIECE`, the algorithm:

#### A. Determines Last Activity Date (Lines 182-196)
- Extracts `lastPracticeDate` and `lastPerformanceDate` from piece fields
- Determines the most recent activity and its type (practice vs performance)
- Calculates `daysSinceLastActivity`

#### B. Favorite Pieces Logic (Lines 204-220)
**Criteria for inclusion:**
- Piece is marked as favorite (`piece.isFavorite == true`)
- Last activity was 2+ days ago OR never practiced (`lastActivityDate < twoDaysAgo`)
- Not practiced today (`lastActivityDate < startOfToday`)

**Time Constants:**
- `twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)`
- `startOfToday = midnight of current day`

**Suggestion Reason Format:**
- Never practiced: `"Favorite piece - Never practiced"`
- Has activity: `"Favorite piece - Last [practice/performance] X days ago"`

#### C. Non-Favorite Pieces Logic (Lines 221-237)
**Criteria for inclusion:**
- Piece is NOT marked as favorite (`!piece.isFavorite`)
- Has been practiced before (`lastActivityDate != null`)
- Last activity was 7+ days ago (`lastActivityDate < sevenDaysAgo`)
- Last activity was within 31 days (`lastActivityDate >= thirtyOneDaysAgo`)

**Time Constants:**
- `sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)`
- `thirtyOneDaysAgo = now - (31 * 24 * 60 * 60 * 1000L)`

**Suggestion Reason Format:**
- `"Last [practice/performance] X days ago"`

### 3. Favorite Suggestions Limiting & Fallback (Lines 240-306)

#### A. Primary Limit Check (Lines 241-243)
```kotlin
val favoriteLimit = if (proUserManager.isProUser()) 
    ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS // 4
    else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS // 1
```

#### B. Fallback Logic (Lines 246-305)
**Trigger:** If fewer favorites found than limit allows
**Purpose:** Fill remaining slots with additional favorites

**Fallback Criteria:**
- All favorite pieces of type `ItemType.PIECE`
- Exclude pieces already selected
- Exclude pieces practiced today (`lastActivityDate >= startOfToday`)

**Fallback Selection Algorithm:**
1. Calculate `daysSinceLastActivity` for each candidate
2. Select least recently practiced (highest `daysSinceLastActivity`)
3. For ties: earliest timestamp first, then alphabetical by name
4. Take up to needed count to reach limit

**Critical Issue:** This fallback can add favorites that don't meet the 2-day criteria.

### 4. Non-Favorite Suggestions Limiting & Fallback (Lines 308-373)

#### A. Primary Limit Check (Lines 309-311)
```kotlin
val nonFavoriteLimit = if (proUserManager.isProUser()) 
    ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS // 4
    else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS // 2
```

#### B. Fallback Logic (Lines 314-372)
**Trigger:** If fewer non-favorites found than limit allows
**Purpose:** Fill remaining slots with "abandoned" pieces

**Fallback Criteria:**
- All non-favorite pieces of type `ItemType.PIECE`
- Exclude pieces already selected
- Only include "abandoned" pieces: never practiced OR 31+ days since last activity
- Time check: `lastActivityDate == null || lastActivityDate < thirtyOneDaysAgo`

**Fallback Selection Algorithm:**
1. Calculate `daysSinceLastActivity` for each abandoned piece
2. Select most recently practiced among abandoned (lowest `daysSinceLastActivity`)
3. For ties: latest timestamp first, then alphabetical by name
4. Take up to needed count to reach limit

**Critical Issue:** This significantly expands the pool beyond the core 7-31 day window.

### 5. Final Assembly (Lines 375-378)
```kotlin
return (finalFavoriteSuggestions + finalNonFavoriteSuggestions).map {
    it.copy(suggestionType = SuggestionType.PRACTICE)
}
```
Combines favorites and non-favorites, ensuring all have `PRACTICE` suggestion type.

## User Limits

### Pro Users
- **Favorite Suggestions:** 4 (`PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS`)
- **Non-Favorite Suggestions:** 4 (`PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS`)
- **Total Practice Suggestions:** Up to 8

### Free Users
- **Favorite Suggestions:** 1 (`FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS`)
- **Non-Favorite Suggestions:** 2 (`FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS`)
- **Total Practice Suggestions:** Up to 3

## Critical Issues Identified

### 1. Fallback Logic Overrides Core Criteria
**Problem:** The fallback mechanisms can add suggestions that don't meet the intended time-based criteria.

**Favorite Fallback Issue:**
- Core criteria: 2+ days since last activity
- Fallback criteria: Any favorite not practiced today
- Result: Can include favorites practiced 1 day ago

**Non-Favorite Fallback Issue:**
- Core criteria: 7-31 days since last activity
- Fallback criteria: 31+ days or never practiced
- Result: Can include pieces outside the intended 7-31 day "sweet spot"

### 2. Complex Interaction with generateAllSuggestions()
When `generatePracticeSuggestions()` is called from `generateAllSuggestions()`, additional limiting is applied:
```kotlin
val finalNonFavoriteLimit = PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS + PRO_USER_PERFORMANCE_SUGGESTIONS // 4 + 5 = 9
```
This can allow more suggestions than the individual method intends.

### 3. Inconsistent Time Window Logic
**Favorites:** Use 2-day threshold with fallback to any time
**Non-Favorites:** Use 7-31 day window with fallback to 31+ days
**Result:** Different expansion behaviors for different suggestion types

## Algorithm Behavior Analysis

### Expected Behavior (Based on Constants)
- **Pro Users:** 4 favorite + 4 non-favorite = 8 practice suggestions
- **Free Users:** 1 favorite + 2 non-favorite = 3 practice suggestions

### Actual Behavior (With Fallbacks)
- Can generate up to the limits, but may include pieces outside intended criteria
- Fallback logic ensures limits are reached even with insufficient "qualifying" pieces
- When combined with `generateAllSuggestions()` limiting, can exceed individual limits

### Performance Characteristics
- **Time Complexity:** O(n) where n = number of pieces (single pass through pieces list)
- **Space Complexity:** O(k) where k = number of suggestions generated (typically 8 for Pro, 3 for Free)
- **Database Queries:** None (uses cached piece fields for last activity dates)

## Recommendations for Investigation

1. **Verify Fallback Necessity:** Are the fallback mechanisms actually needed, or do they over-expand the suggestion pool?

2. **Analyze Time Window Effectiveness:** Are the 2-day (favorites) and 7-31 day (non-favorites) windows appropriate?

3. **Review Combined Limiting:** Should `generateAllSuggestions()` apply additional limits beyond individual method limits?

4. **Consider User Experience:** Do users expect strict time-based criteria, or is the expanded fallback pool beneficial?

5. **Measure Real-World Usage:** How often do the fallback mechanisms activate in production data?

## Test Cases for Verification

### Scenario 1: Sufficient Qualifying Pieces
- **Setup:** Pro user with 4+ favorites (2+ days old) and 4+ non-favorites (7-31 days old)
- **Expected:** Exactly 4 favorites + 4 non-favorites, no fallback activation
- **Verify:** All suggestions meet strict time criteria

### Scenario 2: Insufficient Favorites
- **Setup:** Pro user with 2 favorites meeting criteria, many favorites not meeting criteria
- **Expected:** 2 strict favorites + 2 fallback favorites = 4 total
- **Verify:** Fallback favorites don't meet 2-day criteria

### Scenario 3: Insufficient Non-Favorites
- **Setup:** Pro user with 2 non-favorites in 7-31 day window, many abandoned pieces
- **Expected:** 2 strict non-favorites + 2 abandoned pieces = 4 total
- **Verify:** Abandoned pieces are 31+ days old or never practiced

### Scenario 4: Edge Case - All Pieces Practiced Today
- **Setup:** All pieces have `lastActivityDate >= startOfToday`
- **Expected:** No suggestions generated (all excluded)
- **Verify:** Empty suggestion list returned