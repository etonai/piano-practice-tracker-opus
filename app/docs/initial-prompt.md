# Initial Project Prompt

This document contains the original prompt that was provided to Claude Opus, Claude Sonnet, ChatGPT, and Cursor at the beginning of the Music Practice Tracker project development.

---

## Original User Requirements

I am an experienced software developer. My primary experience is in back end java and C# services and applications, but I also have some front end experience.

I wish to develop an android app that will help me track my piano practice and performances. I know nothing about developing android apps.

The primary function of this app will be to track my performances and practices. Whenever I practice or perform a piece or technique, I want to enter that into my phone, along with the "level" that I performed or practiced that piece. Pieces/techniques I have practiced or performed in the past should be available as a dropdown list. I should also have the ability to enter in a new piece or technique if I haven't tracked working on it before.

I should also be able to import/export the data to a csv file. I would like to know my options for how to do that. I think I will primarily import/export from my google drive account.

### Practice Levels:
- **1** - essentials
- **2** - incomplete practice
- **3** - complete practice, with errors or going back over portions
- **4** - perfect complete practice

### Performance levels:
- **1** - Failed performance
- **2** - Unsatisfactory performance
- **3** - Satisfactory performance

Performances may be either online or live.

For practice level 3, please come up with a better way to describe this type of practice. It is when I perform the piece in its entirety, but either I have some unsatisfactory errors, or I stop and go over parts of the piece to more thoroughly practice that section.

The way I envision using this application is that I will have my phone next to me as I practice.

## Original Application Flow Design

Here is my first thought on the flow of the application.

### 0000 - Main Page
Has four buttons:
- View Progress
- Add Practice
- Add Performance
- Adjust favorites

### 1000 - View Progress
I would like some feedback on how you think I should do this.

Ideas:
- One idea is that the user can either select by date or select by piece/technique
- Select by date would show a list of recent dates and show which pieces/techniques have been performed or practiced
- Select by piece/technique would allow the user to select the list of pieces/techniques and then the app would show, by date, all of the performances and practices of that.
- One idea is that it basically displays a spreadsheet of the pieces that have been performed/practiced in the last few days
- One idea is that the app can just display a calendar, and have different colors for days where any practices/performances happened vs days where no practices/performances happened
- For days where practices/performances happened, we can have brighter colors if more pieces/techniques were performed/practiced

The user also has a back button to go back to the main page (0000)

### 2000 - Add Practice
Immediately goes to selecting the piece/technique list 2100. The user also has a back button to go back to the main page (0000)

### 2100 - Select Piece/Technique
Is a list of all pieces/techniques in the system. 
- There is a button at the top to add a new piece/technique (if selected 2110)
- There are actually two lists. The first list is the "Favorites" list. The second list is alphabetical of all available pieces/techniques
- The user also has a back button to go back to Add practice (2000)

### 2110 - Add piece/technique
- User types in the name of the piece/technique. The user does not have an option to say whether it is a piece or technique - they are equivalent.
- Once the user hits ok, move on to selecting the practice level (2200)
- The user also has a back button to back to Select Piece/Technique (2100)

### 2200 - Select the practice level
This is a list of the possible practice levels. Once the user selects the practice level, the user is given the summary and an ok button. The user also has a back button to go either to 2100 or 2110, depending on the last page.

### 2300 - Summary practice page
Shows the piece/technique and the level. The user can either hit OK or go back to the previous page. The user can also cancel and go back to the main page.

### 3000 - Add Performance
Has two buttons:
- Online Performance
- Live Performance

Once the type of performance is selected, the user goes to a page to select the level of performance. This is very similar to the pages that adding practices uses (all of the 2000-2999 pages), except the performance levels are different

### 4000 - Adjust favorites
The user can add new favorites or unfavorite. The user has a back button to go back to the main page 0000

### 4100 - Add new favorites
- The user has a back button to go back to 4000
- The user has a list of all pieces, alphabetically ordered. The favorited pieces are highlighted. Pressing on a piece will toggle between being highlighted and unlighted.
- The user can press ok, and the favorites will be updated.

### 4200 - Unfavorite
- The user has a back button to go back to 4000.
- The user has a list of all favorited pieces, in alphabetical order, and all highlighted. Pressing on a piece will toggle between being highlighted and unhighlighted.
- Please note that 4100 and 4200 use very similar functionality. The only difference is the initial population of the list of pieces/techniques we see.
- The user can press ok, and the favorites will be updated.

## Future Ideas:
- Allow the app to show pieces that haven't been performed/practiced lately
- Allow the user to select the difference between a piece and a technique. Techniques will never be shown to the user when the user Adds a performance (3000)

## Questions:
- Is there a better way for me to describe a practice/performance than "practice/performance"
- What is the best way to view my progress on all of my pieces?

---

## Implementation Notes

This original prompt served as the foundation for the Music Practice Tracker application. The final implementation evolved significantly from this initial design, incorporating modern Android development practices and user experience improvements suggested by the AI development tools (Claude Opus, Claude Sonnet, ChatGPT, and Cursor).

### Key Changes from Original Design:
- **Navigation**: Moved from numbered page flow to modern Android Navigation Component with fragments
- **UI Structure**: Implemented bottom navigation tabs instead of button-based main page
- **Progress Viewing**: Combined calendar and timeline views for comprehensive progress tracking
- **Data Management**: Enhanced CSV import/export with Unicode normalization and favorites preservation
- **Architecture**: Applied MVVM pattern with Room database and Repository pattern

The collaborative AI-assisted development process resulted in a more robust and user-friendly application while maintaining the core functionality described in this original prompt.