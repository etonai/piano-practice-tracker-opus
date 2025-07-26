# Development Tickets

This document tracks feature requests, enhancements, and larger development tasks that are not yet ready for implementation but should be considered for future development cycles.

## Status Legend
- 🎫 **Open** - Ticket is available for planning and implementation
- 🔄 **In Progress** - Ticket is being actively worked on
- 🔍 **In Verification** - Implementation complete, awaiting user verification
- ✅ **Completed** - Ticket has been implemented and verified by user
- ❌ **Closed** - Ticket closed without implementation (e.g., not feasible, duplicate, obsolete)

## Open Tickets

### Ticket #1: 🎫 Expand Firebase Analytics Event Types
**Status:** Open  
**Date Created:** 2025-07-23  
**Priority:** Medium  
**Description:** Expand current Firebase Analytics to track additional event types beyond basic activity logging for deeper user behavior insights.  
**Details:** [ticket_1.md](ticketdetail/ticket_1.md)





---

## Completed Tickets

### Ticket #8: ✅ Fix Empty Suggestions Screen Text and Remove Pro References
**Status:** Completed  
**Date Created:** 2025-07-26  
**Date Completed:** 2025-07-26  
**Priority:** High  
**Description:** When no suggestions appear, the empty state text contains incorrect explanations and mentions "Pro users" which should not appear in the free version.  
**Details:** [ticket_8.md](ticketdetail/ticket_8.md)

### Ticket #7: ✅ Practice Duration Default Shows 15 But Doesn't Save
**Status:** Completed  
**Date Created:** 2025-07-25  
**Date Completed:** 2025-07-26  
**Priority:** High  
**Description:** When adding practice, duration field shows "15" as placeholder but saves empty value if not edited. Change default to 5 minutes and save that value.  
**Details:** [ticket_7.md](ticketdetail/ticket_7.md)

### Ticket #6: ✅ Revise Practice Level Descriptions
**Status:** Completed  
**Date Created:** 2025-07-25  
**Date Completed:** 2025-07-26  
**Priority:** Medium  
**Description:** Update practice level descriptions: Level 3 to "Complete with Issues" and Level 4 to "Complete and Satisfactory".  
**Details:** [ticket_6.md](ticketdetail/ticket_6.md)

### Ticket #5: ✅ Delete Piece with Trash Icon in Pieces Tab
**Status:** Completed  
**Date Created:** 2025-07-24  
**Date Completed:** 2025-07-24  
**Priority:** Medium  
**Description:** In the Pieces tab, add a trash icon for each piece so that the user can delete pieces. Deleting a piece should also delete all activities associated with that piece.  
**Details:** [ticket_5.md](ticketdetail/ticket_5.md)

### Ticket #3: ✅ Reduce Debug Logging for Performance Suggestions
**Status:** Completed  
**Date Created:** 2025-07-24  
**Date Completed:** 2025-07-24  
**Priority:** Low  
**Description:** Remove excessive debug logging for performance suggestions in DashboardFragment to clean up log output and improve development experience.  
**Details:** [ticket_3.md](ticketdetail/ticket_3.md)

### Ticket #4: ✅ Activity Limit Not Enforced on Manual Add
**Status:** Completed  
**Date Created:** 2025-07-24  
**Date Completed:** 2025-07-24  
**Priority:** High  
**Description:** Adding a new activity does not stop if the user is beyond the activity limit. The activity limit only prevents importing more activities, not manual addition.  
**Details:** [ticket_4.md](ticketdetail/ticket_4.md)

### Ticket #2: ✅ Bug - Practice Suggestions Auto-Add 30 Minutes Duration
**Status:** Completed  
**Date Created:** 2025-07-23  
**Date Completed:** 2025-07-23  
**Priority:** High  
**Description:** Quick-add from practice suggestions automatically sets 30 minutes duration instead of leaving field empty for user input.  
**Details:** [ticket_2.md](ticketdetail/ticket_2.md)

---

## Closed Tickets

*No closed tickets yet*

---

## Ticket Template

```markdown
### Ticket #[Number]: 🎫 [Title]
**Status:** [Open/In Progress/In Verification/Completed/Closed]  
**Date Created:** YYYY-MM-DD  
**Priority:** [Critical/High/Medium/Low]  
**Description:** [One line summary]  
**Details:** [ticket_#.md](ticketdetail/ticket_#.md)
```