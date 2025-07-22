# PlayStreak Product Specification (Original)

**Version:** 1.0.0  
**Date:** July 22, 2025  
**Status:** ARCHIVED - For Reference Only
**Author:** Claude Code Assistant (Opus model)

> **Important Note:** This is an archived copy of the original product specification. This document is identical to `product-specification.md` which has been superseded by `product-specification-actual.md`. The original specification contains many features that were not implemented (batch entry, analytics, multiple import sources, etc.) and should be considered a speculative vision document rather than an accurate specification of the existing product.
>
> **For accurate documentation of PlayStreak as built, see `product-specification-actual.md`**

## Executive Summary

PlayStreak is a mobile application designed to help musicians maintain consistent practice habits through intelligent tracking, visualization, and gamification. The app targets both casual musicians and serious practitioners, offering a free tier with comprehensive tracking capabilities and a premium tier with advanced analytics and convenience features.

## Table of Contents

1. [Product Vision](#product-vision)
2. [Target Audience](#target-audience)
3. [Core Concepts](#core-concepts)
4. [User Stories](#user-stories)
5. [Feature Specifications](#feature-specifications)
6. [User Interface Design](#user-interface-design)
7. [Data Model](#data-model)
8. [Business Model](#business-model)
9. [Technical Requirements](#technical-requirements)
10. [Success Metrics](#success-metrics)

---

## Product Vision

### Mission Statement
"Empower musicians of all levels to build and maintain consistent practice habits through intelligent tracking, meaningful insights, and motivational feedback."

### Core Values
- **Accessibility**: Core functionality available to all users
- **Simplicity**: Complex data presented intuitively
- **Motivation**: Encourage daily practice through positive reinforcement
- **Flexibility**: Adapt to different practice styles and instruments
- **Privacy**: User data remains private and portable

### Key Differentiators
1. **Streak-based motivation** - Visual feedback for consistent practice
2. **Intelligent suggestions** - Data-driven practice recommendations
3. **Teacher-friendly** - Features supporting music education
4. **Data portability** - Import/export capabilities
5. **Offline-first** - Full functionality without internet

---

## Target Audience

### Primary Users

#### 1. Music Students (Age 8-22)
- Learning one or more instruments
- Need motivation to practice regularly
- Track progress for personal improvement
- May share screenshots with teachers/parents
- **Free tier primary target**

#### 2. Adult Hobbyists (Age 23-65)
- Self-directed learners
- Limited practice time
- Focus on enjoyment and progress
- Want simple, effective tracking
- **Mix of free and pro users**

#### 3. Serious Musicians (Age 16+)
- Professional or pre-professional
- Multiple pieces in active rotation
- Need detailed analytics
- Track performance preparation
- **Pro tier primary target**

### Secondary Users

#### 4. Music Teachers (Personal Use)
- Use app to track their OWN practice
- May recommend app to students
- Each student would have their own account
- **Not for managing student data**

### User Personas

**Emma - High School Student**
- 16 years old, plays violin
- Preparing for college auditions
- Practices 1-2 hours daily
- Needs: Repertoire management, streak tracking
- Shows progress to teacher via screenshots

**David - Adult Beginner**
- 35 years old, learning piano
- 30 minutes practice, 3-4 times/week
- Needs: Simple tracking, motivation
- Wants to see improvement over time

**Sarah - Professional Pianist**
- 28 years old, performing artist
- Manages 20+ active pieces
- Needs: Advanced analytics, quick logging
- Tracks performances and practice efficiency

**Marcus - Jazz Guitarist**
- 22 years old, music college student
- Practices technique and improvisation
- Needs: Track both pieces and exercises
- Exports data for semester reviews

---

## Core Concepts

### 1. Activities
The fundamental unit of tracking. An activity represents a single practice session or performance.

**Activity Types:**
- **Practice Session**: Regular practice with duration tracking
- **Performance**: Concerts, recitals, auditions

**Activity Attributes:**
- Date and time
- Piece/technique practiced
- Duration (for practice)
- Level (1-4 for practice, 1-3 for performance)
- Notes (optional)
- Performance type (for performances)

### 2. Pieces and Techniques
Items that can be practiced or performed.

**Types:**
- **Piece**: Musical composition (e.g., "Moonlight Sonata")
- **Technique**: Practice exercise (e.g., "C Major Scales")

**Attributes:**
- Name
- Type (piece/technique)
- Favorite status
- Activity history
- Last practiced date

### 3. Practice Levels

**For Practice Sessions:**
1. **Level 1 - Essentials**: Basic run-through, sight-reading
2. **Level 2 - Incomplete**: Partial practice, specific sections
3. **Level 3 - Complete with Review**: Full piece with corrections
4. **Level 4 - Perfect Complete**: Performance-ready practice

**For Performances:**
1. **Level 1 - Failed**: Significant errors or incomplete
2. **Level 2 - Unsatisfactory**: Completed but below standards
3. **Level 3 - Satisfactory**: Successful performance

### 4. PlayStreak
Consecutive days of practice. The core motivational mechanic.

**Rules:**
- Any activity counts toward streak
- Midnight-to-midnight calculation
- Timezone aware
- Visual indicators for current streak

### 5. Suggestions
Intelligent recommendations for what to practice based on:
- Time since last practice
- Favorite status
- Practice patterns
- Neglected pieces

---

## User Stories

### Core User Stories (Free Tier)

1. **As a musician, I want to quickly log my practice sessions so I can track my progress**
   - One-tap access to add activity
   - Recent pieces for quick selection
   - Optional duration and notes

2. **As a student, I want to see my practice streak so I stay motivated**
   - Prominent streak display
   - Visual calendar heat map
   - Celebration for milestones

3. **As a musician, I want to track multiple pieces so I can manage my repertoire**
   - Add/edit/organize pieces
   - Mark favorites (up to 4 for free)
   - See practice history per piece

4. **As a user, I want to see my practice history so I can reflect on my progress**
   - Timeline view of all activities
   - Filter by date range
   - Detailed activity information

5. **As a user, I want practice suggestions so I know what needs attention**
   - 3 daily suggestions (1 favorite, 2 regular)
   - Based on practice patterns
   - Clear reasoning for each suggestion

### Premium User Stories (Pro Tier)

6. **As a pro user, I want advanced practice analytics so I can optimize my practice**
   - Detailed heat maps with intensity
   - Practice efficiency metrics
   - Trend analysis

7. **As a pro user, I want to import my practice history so I can migrate from other apps**
   - Unlimited CSV imports
   - Advanced field mapping
   - Import from multiple sources

8. **As a pro user, I want unlimited favorites so I can track my full active repertoire**
   - No limits on favorites
   - Enhanced organization tools
   - Quick access features

9. **As a pro user, I want quick-add shortcuts so I can log activities faster**
   - One-tap add from suggestions
   - One-tap add from piece lists
   - Batch activity entry

---

## Feature Specifications

### Dashboard (Free & Pro)

**Purpose**: Central hub showing current status and quick actions

**Free Features:**
- Current streak display (large, prominent)
- Today's practice summary
- 3 practice suggestions
- Quick access buttons (Add Activity, Add Piece)
- Week summary statistics

**Pro Enhancements:**
- 8 practice suggestions (4 favorites, 4 regular)
- Practice efficiency score
- Trending insights
- Quick-add buttons on suggestions

### Calendar View (Free & Pro)

**Purpose**: Visual representation of practice patterns

**Free Features:**
- Monthly calendar view
- Simple heat map (practiced/not practiced)
- Day selection for details
- Previous/Next navigation
- Current month/year display

**Pro Enhancements:**
- Intensity-based heat map (6 color levels)
- Activity type indicators
- Practice statistics overlay
- Year-view option

### Activity Management (Free & Pro)

**Purpose**: Log and manage practice sessions and performances

**Free Features:**
- Add practice/performance
- Select from existing pieces
- Add new pieces inline
- 4 practice levels, 3 performance levels
- Duration tracking (practice)
- Notes field
- Edit/delete activities

**Pro Enhancements:**
- Quick-add from any list
- Batch activity entry
- Activity templates
- Voice notes (future)
- Photo attachments (future)

### Piece Management (Free & Pro)

**Purpose**: Organize repertoire and practice materials

**Free Features:**
- Add/edit/delete pieces
- Piece vs technique designation
- Mark up to 4 favorites
- Sort by name/date/activity count
- Search functionality
- Practice history per piece

**Pro Enhancements:**
- Unlimited favorites
- Advanced categorization
- Repertoire lists
- Inactive piece tracking
- Bulk operations

### Timeline (Free & Pro)

**Purpose**: Chronological view of all activities

**Free Features:**
- Reverse chronological list
- Activity type indicators
- Detailed view on tap
- Delete activities
- Day grouping

**Pro Features:**
- Advanced filtering
- Export timeline
- Activity statistics
- Batch operations

### Data Management (Free & Pro)

**Purpose**: Import/export and backup capabilities

**Free Features:**
- Export to CSV (unlimited)
- Basic CSV import (1000 activities/month)
- Local data storage
- Manual Google Drive backup

**Pro Enhancements:**
- Unlimited imports
- Advanced import mapping
- Import history/rollback
- Automatic cloud backup
- Multiple format support

### Settings (Free & Pro)

**Purpose**: App configuration and account management

**Free Features:**
- Add Activity button
- Add Piece button (with count)
- Manage Favorites button
- Import/Export access
- App version display

**Pro Enhancements:**
- "Pro" badge in title
- Advanced settings
- Data analytics
- Backup preferences

---

## User Interface Design

### Design Principles

1. **Clarity First**: Every element has clear purpose
2. **Thumb-Friendly**: Primary actions within thumb reach
3. **Consistent Patterns**: Similar actions use similar UI
4. **Progressive Disclosure**: Advanced features don't clutter basics
5. **Delightful Details**: Celebrations for achievements

### Navigation Structure

```
Bottom Navigation (5-6 tabs)
├── Dashboard (home)
├── Calendar
├── Suggestions
├── Pieces
├── Timeline
└── [Inactive] (Pro only)

Settings (accessible from Dashboard)
```

### Color Scheme

**Primary Colors:**
- Primary: Material Blue (#2196F3)
- Accent: Amber (#FFC107)
- Success: Green (#4CAF50)
- Error: Red (#F44336)

**Semantic Colors:**
- Practice: Blue tones
- Performance: Purple tones
- Favorite: Gold/Yellow
- Streak: Orange/Red gradient

### Key UI Components

**1. Streak Display**
- Large number with flame icon
- Animated on increment
- Celebration at milestones (7, 30, 100, 365)

**2. Activity Cards**
- Clean white cards with shadows
- Type indicator (practice/performance)
- Duration and level badges
- Swipe actions (Pro)

**3. Heat Map**
- Square day cells
- Color intensity (Pro)
- Month navigation
- Today indicator

**4. Floating Action Button**
- Primary: Add Activity
- Contextual quick actions

**5. Empty States**
- Helpful illustrations
- Clear call-to-action
- Encouraging messages

---

## Data Model

### Core Entities

```sql
-- Pieces and Techniques
CREATE TABLE pieces (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT CHECK(type IN ('PIECE', 'TECHNIQUE')),
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Practice Activities
CREATE TABLE activities (
    id INTEGER PRIMARY KEY,
    piece_id INTEGER REFERENCES pieces(id),
    activity_type TEXT CHECK(activity_type IN ('PRACTICE', 'PERFORMANCE')),
    level INTEGER,
    performance_type TEXT,
    duration_minutes INTEGER,
    notes TEXT,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Preferences
CREATE TABLE preferences (
    key TEXT PRIMARY KEY,
    value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Key Relationships
- One piece → Many activities
- Activities ordered by timestamp
- Preferences store Pro status, settings

### Data Constraints

**Free Users:**
- Maximum 4 favorites
- 500 total activities
- 50 active pieces

**Pro Users:**
- Unlimited favorites
- Unlimited activities
- Unlimited pieces

---

## Business Model

### Monetization Strategy

**Free Tier: "PlayStreak Free"**
- Full core functionality
- Basic limits (4 favorites, 3 suggestions)
- Perfect for students and casual users
- No ads, no time limits

**Premium Tier: "PlayStreak Pro" ($4.99/month or $39.99/year)**
- Unlimited favorites
- Advanced analytics
- Quick-add features
- Import enhancements
- Priority support

### Pricing Strategy
- **Individual**: $4.99/month, $39.99/year (save 33%)
- **Student**: $2.99/month (with verification)
- **Family**: $9.99/month (up to 5 separate accounts)
- **Bulk Licenses**: Custom pricing for schools/institutions

### Conversion Strategy
1. **Free Trial**: 14-day Pro trial
2. **Feature Discovery**: Gentle prompts when limits reached
3. **Value Demonstration**: Show what Pro analytics reveal
4. **Social Proof**: Teacher testimonials
5. **Seasonal Promotions**: Back-to-school, New Year

---

## Technical Requirements

### Platform Requirements

**Android:**
- Minimum SDK: 24 (Android 7.0)
- Target SDK: Latest stable
- Kotlin language
- Material Design 3

**iOS (Future):**
- iOS 15+
- Swift/SwiftUI
- Native design language

### Architecture Requirements
- MVVM architecture pattern
- Repository pattern for data
- Dependency injection (Dagger Hilt)
- Reactive programming (Flow/LiveData)
- Offline-first design

### Data Requirements
- Local SQLite database (Room)
- Cloud sync (Google Drive/iCloud)
- CSV import/export
- JSON backup format
- End-to-end encryption for cloud

### Performance Requirements
- App launch: <2 seconds
- Activity save: <500ms
- Data sync: Background
- Offline capability: 100%
- Battery efficient

### Security Requirements
- No PII collected without consent
- Local data encryption option
- Secure cloud transmission
- GDPR/COPPA compliant
- Regular security audits

---

## Success Metrics

### User Engagement
- **Daily Active Users**: 60% of monthly
- **Streak Retention**: 40% maintain 7-day streak
- **Feature Adoption**: 80% use suggestions

### Business Metrics
- **Conversion Rate**: 5% free to Pro
- **Retention**: 80% monthly Pro retention
- **Revenue per User**: $2.50 monthly average

### Quality Metrics
- **Crash Rate**: <0.5%
- **App Rating**: 4.5+ stars
- **Support Tickets**: <2% of users
- **Performance**: 99.9% uptime

### Growth Metrics
- **Monthly Growth**: 15% MoM
- **Referral Rate**: 20% recommend to friends
- **App Store Ranking**: Top 10 in Music category

---

## Appendices

### A. Competitive Analysis
- **Tonal Energy**: Practice tools focus
- **Music Journal**: Simple logging
- **Modacity**: Professional features
- **PlayStreak Advantage**: Better UX, fair pricing

### B. Future Features
- Social features (share achievements)
- AI practice recommendations
- Audio/video recording
- Sheet music integration
- Metronome/tuner integration
- Practice room booking

### C. Launch Strategy
1. Beta with serious musicians
2. Music school student outreach
3. App store optimization
4. Content marketing (practice tips)
5. Community building (practice challenges)

---

*End of Product Specification Document*