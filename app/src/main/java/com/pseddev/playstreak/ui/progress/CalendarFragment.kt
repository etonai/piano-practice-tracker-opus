package com.pseddev.playstreak.ui.progress

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.databinding.FragmentCalendarBinding
import com.pseddev.playstreak.utils.ProUserManager
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.*

class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository
        )
    }
    
    private var selectedDate: LocalDate? = null
    private var monthlyActivities: Map<LocalDate, List<ActivityWithPiece>> = emptyMap()
    private var currentDisplayMonth: YearMonth = YearMonth.now()
    private lateinit var proUserManager: ProUserManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        proUserManager = ProUserManager.getInstance(requireContext())
        
        setupClickListeners()
        setupCalendar()
        updateMonthDisplay()
        updateColorGuideVisibility()
        
        viewModel.selectedDateActivities.observe(viewLifecycleOwner) { activities ->
            updateSelectedDateView(activities)
        }
        
        viewModel.monthlyActivitySummary.observe(viewLifecycleOwner) { summary ->
            updateMonthlySummary(summary)
            updateMonthlyActivities(summary)
        }
        
        // Set initial date to today
        val today = LocalDate.now()
        selectedDate = today
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModel.selectDate(todayMillis)
    }
    
    private fun setupCalendar() {
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.bind(data)
            }
        }
        
        // Set up calendar to show current month
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        binding.calendarView.setup(firstMonth, lastMonth, com.kizitonwose.calendar.core.firstDayOfWeekFromLocale())
        binding.calendarView.scrollToMonth(currentMonth)
        
        // Disable month swiping by intercepting horizontal scroll gestures
        disableCalendarSwiping()
    }
    
    private fun disableCalendarSwiping() {
        var initialX = 0f
        
        binding.calendarView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Store the initial touch position for swipe detection
                    initialX = event.x
                    false // Allow the touch to proceed for day selection
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = kotlin.math.abs(event.x - initialX)
                    
                    // Block horizontal swipes greater than 30 pixels to prevent month changes
                    // Allow smaller movements for day selection and natural touch behavior
                    if (deltaX > 30) {
                        true // Consume horizontal swipes to prevent month navigation
                    } else {
                        false // Allow day selection and small movements
                    }
                }
                else -> false // Allow all other touch events (UP, CANCEL, etc.)
            }
        }
    }
    
    private fun updateMonthlyActivities(summary: MonthlyActivitySummary) {
        // Convert activities map to LocalDate keys
        monthlyActivities = summary.dailyActivities.mapKeys { (dateMillis, _) ->
            val instant = java.time.Instant.ofEpochMilli(dateMillis)
            instant.atZone(ZoneId.systemDefault()).toLocalDate()
        }
        
        // Refresh calendar to update colors
        binding.calendarView.notifyCalendarChanged()
    }
    
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        
        
        fun bind(day: CalendarDay) {
            textView.text = day.date.dayOfMonth.toString()
            
            if (day.position == DayPosition.MonthDate) {
                textView.visibility = View.VISIBLE
                
                // Get activities for this date
                val activities = monthlyActivities[day.date] ?: emptyList()
                val color = getCalendarColorForActivities(activities)
                
                // Set background color
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_day_background)?.mutate() as? GradientDrawable
                drawable?.setColor(color)
                textView.background = drawable
                
                // Set text color for visibility
                textView.setTextColor(getTextColorForBackground(color))
                
                // Handle selection
                textView.setOnClickListener {
                    selectedDate = day.date
                    val millis = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    viewModel.selectDate(millis)
                    binding.calendarView.notifyCalendarChanged()
                }
                
                // Highlight selected date
                if (day.date == selectedDate) {
                    // For selected date, use a contrasting color and add border
                    textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                    textView.alpha = 1.0f
                    
                    // Add border to selected date
                    val selectedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_day_background)?.mutate() as? GradientDrawable
                    selectedDrawable?.setColor(color)
                    selectedDrawable?.setStroke(9, ContextCompat.getColor(requireContext(), R.color.calendar_selection_ring))
                    textView.background = selectedDrawable
                } else {
                    textView.alpha = 0.8f
                }
            } else {
                textView.visibility = View.INVISIBLE
            }
        }
    }
    
    private fun updateSelectedDateView(activities: List<ActivityWithPiece>) {
        // Update color indicator with larger, more prominent display
        val color = getCalendarColorForActivities(activities)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle_indicator)?.mutate()
        drawable?.setTint(color)
        binding.activityColorIndicator.background = drawable
        
        // Format the selected date
        val dateText = selectedDate?.let { date ->
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
                    "$monthName ${date.dayOfMonth}"
                }
            }
        } ?: "Selected Date"
        
        if (activities.isEmpty()) {
            binding.selectedDateText.text = "$dateText: No activities on this date"
            binding.selectedDateActivities.text = ""
            binding.activityColorIndicator.visibility = View.GONE
        } else {
            val activityTypeText = getActivityTypeDescription(activities)
            val activityWord = if (activities.size == 1) "activity" else "activities"
            binding.selectedDateText.text = "$dateText: ${activities.size} $activityWord ($activityTypeText)"
            binding.activityColorIndicator.visibility = View.VISIBLE
            
            val summary = activities.joinToString("\n") { item ->
                val time = android.text.format.DateFormat.format("h:mm a", item.activity.timestamp)
                val type = item.activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }
                val level = "(${item.activity.level})"
                val minutes = if (item.activity.minutes > 0) " - ${item.activity.minutes} min" else ""
                "$time - ${item.pieceOrTechnique.name} - $type $level$minutes"
            }
            binding.selectedDateActivities.text = summary
        }
    }
    
    private fun updateMonthlySummary(summary: MonthlyActivitySummary) {
        binding.monthlyActiveDaysText.text = summary.activeDays.toString()
        binding.monthlyActivitiesText.text = summary.totalActivities.toString()
    }
    
    private fun getActivityTypeDescription(activities: List<ActivityWithPiece>): String {
        val hasPerformance = activities.any { it.activity.activityType == ActivityType.PERFORMANCE }
        val practiceCount = activities.count { it.activity.activityType == ActivityType.PRACTICE }
        val performanceCount = activities.count { it.activity.activityType == ActivityType.PERFORMANCE }
        
        return when {
            hasPerformance && practiceCount > 0 -> "Mixed"
            hasPerformance -> "Performance"
            else -> "Practice"
        }
    }
    
    private fun getTextColorForBackground(backgroundColor: Int): Int {
        // Calculate if we need light or dark text based on background color
        val red = Color.red(backgroundColor)
        val green = Color.green(backgroundColor)
        val blue = Color.blue(backgroundColor)
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        
        return if (luminance > 0.5) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }
    
    private fun getCalendarColorForActivities(activities: List<ActivityWithPiece>): Int {
        if (activities.isEmpty()) {
            return ContextCompat.getColor(requireContext(), R.color.calendar_no_activity)
        }
        
        // Free users get simplified heat map: only light blue for any activities
        if (!proUserManager.isProUser()) {
            return ContextCompat.getColor(requireContext(), R.color.calendar_practice_light)
        }
        
        // Pro users get full heat map with multiple colors and intensities
        val activityCount = activities.size
        val hasPerformance = activities.any { it.activity.activityType == ActivityType.PERFORMANCE }
        
        return if (hasPerformance) {
            // Performance days use green colors
            when {
                activityCount >= 11 -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_dark)
                activityCount >= 5 -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_light)
            }
        } else {
            // Practice-only days use blue colors
            when {
                activityCount >= 11 -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_dark)
                activityCount >= 5 -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_light)
            }
        }
    }
    
    private fun updateColorGuideVisibility() {
        val isProUser = proUserManager.isProUser()
        binding.colorGuideTitle.visibility = if (isProUser) View.VISIBLE else View.GONE
        binding.colorGuideContainer.visibility = if (isProUser) View.VISIBLE else View.GONE
    }
    
    private fun setupClickListeners() {
        binding.buttonAddActivity.setOnClickListener {
            findNavController().navigate(R.id.action_viewProgressFragment_to_addActivityFragment)
        }
        
        binding.buttonPreviousMonth.setOnClickListener {
            currentDisplayMonth = currentDisplayMonth.minusMonths(1)
            binding.calendarView.scrollToMonth(currentDisplayMonth)
            updateMonthDisplay()
            updateMonthData()
        }
        
        binding.buttonNextMonth.setOnClickListener {
            currentDisplayMonth = currentDisplayMonth.plusMonths(1)
            binding.calendarView.scrollToMonth(currentDisplayMonth)
            updateMonthDisplay()
            updateMonthData()
        }
    }
    
    private fun updateMonthDisplay() {
        val monthName = currentDisplayMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
        binding.monthYearText.text = "$monthName ${currentDisplayMonth.year}"
    }
    
    private fun updateMonthData() {
        // When switching months, try to select the same day number if it exists in the new month
        val dayToSelect = selectedDate?.let { currentSelected ->
            val dayOfMonth = currentSelected.dayOfMonth
            val newMonth = currentDisplayMonth
            
            // Check if the same day number exists in the new month
            if (dayOfMonth <= newMonth.lengthOfMonth()) {
                newMonth.atDay(dayOfMonth)
            } else {
                // If the day doesn't exist (e.g., Feb 30), select the last day of the month
                newMonth.atEndOfMonth()
            }
        } ?: currentDisplayMonth.atDay(1) // Default to first day if no previous selection
        
        // Update the selected date and view model
        selectedDate = dayToSelect
        val millis = dayToSelect.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModel.selectDate(millis)
        
        // Refresh the calendar view to show the new selection
        binding.calendarView.notifyCalendarChanged()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh calendar and color guide in case Pro status changed while away from this fragment
        binding.calendarView.notifyCalendarChanged()
        updateColorGuideVisibility()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}