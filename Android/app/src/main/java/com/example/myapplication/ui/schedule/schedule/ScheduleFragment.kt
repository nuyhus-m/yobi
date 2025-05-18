package com.example.myapplication.ui.schedule.schedule


import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.data.dto.model.ScheduleItemModel
import com.example.myapplication.databinding.FragmentScheduleBinding
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.schedule.schedule.adapter.ScheduleAdapter
import com.example.myapplication.ui.schedule.schedule.viewmodel.ScheduleViewModel
import com.example.myapplication.util.TimeUtils.toEpochMillis
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


@AndroidEntryPoint
class ScheduleFragment : BaseFragment<FragmentScheduleBinding>(
    FragmentScheduleBinding::bind,
    R.layout.fragment_schedule
) {
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var scheduleAdapter: ScheduleAdapter

    private var currentMonth = YearMonth.now()

    private val minMonth = YearMonth.of(2024, 1)
    private val maxMonth = YearMonth.now().plusMonths(1)

    companion object {
        private val START_MONTH = YearMonth.of(2024, 1)
        private const val MAX_DOTS_PER_DAY = 10
        private const val DOTS_PER_ROW = 3
        private const val DOT_MARGIN = 1
        private const val DOT_SIZE = 4
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (scheduleViewModel.selectedDate.value == null) {
            scheduleViewModel.selectDate(LocalDate.now())
        } else {
            scheduleViewModel.reloadCurrentDate()
        }

        setupCalendar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        val thisMonth = YearMonth.now()
        val prevMonth = thisMonth.minusMonths(1)
        val nextMonth = thisMonth.plusMonths(1)

        listOf(prevMonth, thisMonth, nextMonth).forEach { month ->
            val start = month.atDay(1).toEpochMillis()
            val end = month.atEndOfMonth().toEpochMillis()
            scheduleViewModel.getPeriodSchedule(start, end)
        }

        mainViewModel.clientList.observe(viewLifecycleOwner) { clients ->
            scheduleViewModel.setClientColors(clients)
            binding.cv.notifyCalendarChanged()
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("needRefreshSchedule")
            ?.observe(viewLifecycleOwner) { need ->
                if (need == true) {
                    findNavController().currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("needRefreshSchedule")

                    scheduleViewModel.reloadCurrentDate()
                }
            }

    }


    private fun setupObservers() {
        scheduleViewModel.dotMap.observe(viewLifecycleOwner) {
            binding.cv.notifyCalendarChanged()
        }

        scheduleViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val oldDate = scheduleViewModel.selectedDate.value
            refreshDateAppearance(oldDate, date)
        }

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { list ->
            scheduleAdapter.submitList(list)
        }
    }

    private fun setupClickListeners() {
        binding.btnPrevious.setOnClickListener {
            if(currentMonth > minMonth) {
                currentMonth = currentMonth.minusMonths(1)
                binding.cv.smoothScrollToMonth(currentMonth)
                updateMonthTitle(currentMonth)
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentMonth < maxMonth) {
                currentMonth = currentMonth.plusMonths(1)
                binding.cv.smoothScrollToMonth(currentMonth)
                updateMonthTitle(currentMonth)
            }
        }

        binding.btnScheduleAdd.setOnClickListener {
            val action = ScheduleFragmentDirections
                .actionScheduleFragmentToDestScheduleRegisterDialog(
                    visitedDate = scheduleViewModel.selectedDate.value?.toEpochDay() ?: 0L
                )
            findNavController().navigate(action)

        }
    }


    private fun setupCalendar() {
        val calendarView = binding.cv

        updateMonthTitle(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                updateDateAppearance(container, data)

                container.view.setOnClickListener {
                    scheduleViewModel.selectDate(data.date)
                }

                updateScheduleDots(container, data)
            }
        }

        calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            updateMonthTitle(currentMonth)

            val start = month.weekDays.first().first().date.toEpochMillis()
            val end = month.weekDays.last().last().date.toEpochMillis()
            scheduleViewModel.getPeriodSchedule(start, end)

            val prefetchBefore = month.yearMonth.minusMonths(2)
            val prefetchAfter = month.yearMonth.plusMonths(2)

            listOf(prefetchBefore, prefetchAfter).forEach { preMonth ->
                val preStart = preMonth.atDay(1).toEpochMillis()
                val preEnd = preMonth.atEndOfMonth().toEpochMillis()
                scheduleViewModel.getPeriodSchedule(preStart, preEnd)
            }
        }

        // 캘린더 설정
        calendarView.setup(
            START_MONTH,                    // 시작 월
            YearMonth.now().plusMonths(1),  // 종료 월
            daysOfWeek().first()            // 주 시작 요일 (일요일)
        )

        calendarView.scrollToMonth(currentMonth)

        binding.cv.post {
            binding.cv.findFirstVisibleMonth()?.let { month ->
                val start = month.weekDays.first().first().date.toEpochMillis()
                val end = month.weekDays.last().last().date.toEpochMillis()
                scheduleViewModel.getPeriodSchedule(start, end)
            }
        }
    }

    private fun updateDateAppearance(container: DayViewContainer, data: CalendarDay) {
        val textView = container.textView
        textView.background = null

        when {
            data.date == scheduleViewModel.selectedDate.value -> {
                textView.setBackgroundResource(R.drawable.bg_purple_radius_12)
                textView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.white
                    )
                )
            }

            data.position.name == "MonthDate" -> {
                textView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.black
                    )
                )
            }

            else -> {
                textView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.darker_gray
                    )
                )
            }
        }
    }

    private fun updateScheduleDots(container: DayViewContainer, data: CalendarDay) {
        // 1. 도트 컨테이너 초기화
        val dotContainer = container.view.findViewById<LinearLayout>(R.id.dot_container)
        dotContainer.children.forEach { (it as LinearLayout).removeAllViews() }

        // 2. 날짜에 해당하는 클라이언트 ID 리스트 가져오기
        val clientIds = scheduleViewModel.dotMap.value?.get(data.date) ?: emptyList()

        // 3. 도트 View 추가 (최대 10개, 3개씩 나눠서 row1~4에 배치)
        clientIds.take(MAX_DOTS_PER_DAY).forEachIndexed { index, clientId ->
            val dot = createDotView(clientId)

            val rowIndex = index / DOTS_PER_ROW
            val rowId = when (rowIndex) {
                0 -> R.id.row1
                1 -> R.id.row2
                2 -> R.id.row3
                else -> R.id.row4
            }

            val row = container.view.findViewById<LinearLayout>(rowId)
            row.addView(dot)
        }
    }

    private fun createDotView(clientId: Int): AppCompatImageView {
        return AppCompatImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_schedule_dot)
            val color = (scheduleViewModel.clientColorMap[clientId] ?: "#000000").toColorInt()
            imageTintList = ColorStateList.valueOf(color)
            layoutParams = LinearLayout.LayoutParams(DOT_SIZE.dp, DOT_SIZE.dp).apply {
                setMargins(DOT_MARGIN.dp, 0, DOT_MARGIN.dp, 0)
            }
        }
    }

    private fun refreshDateAppearance(oldDate: LocalDate?, newDate: LocalDate?) {
        if (oldDate != null) {
            binding.cv.notifyDateChanged(oldDate)
        }

        if (newDate != null) {
            binding.cv.notifyDateChanged(newDate)
        }

        refreshMonthsIfNeeded(oldDate, newDate)
    }

    private fun refreshMonthsIfNeeded(oldDate: LocalDate?, newDate: LocalDate?) {
        val currentCalendarMonth = binding.cv.findFirstVisibleMonth()?.yearMonth ?: return

        binding.cv.notifyMonthChanged(currentCalendarMonth)

        val nextMonth = currentCalendarMonth.plusMonths(1)
        val prevMonth = currentCalendarMonth.minusMonths(1)

        if (isDateInMonth(oldDate, nextMonth) || isDateInMonth(newDate, nextMonth)) {
            binding.cv.notifyMonthChanged(nextMonth)
        }

        if (isDateInMonth(oldDate, prevMonth) || isDateInMonth(newDate, prevMonth)) {
            binding.cv.notifyMonthChanged(prevMonth)
        }
    }

    private fun isDateInMonth(date: LocalDate?, month: YearMonth): Boolean {
        return date?.let { it.month == month.month } == true
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        val year = yearMonth.year
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        binding.tvDate.text = "${year}년 ${month}"
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(
            emptyList<ScheduleItemModel>(),
            scheduleViewModel,
            onEditClick = { scheduleId ->
                val action = ScheduleFragmentDirections
                    .actionScheduleFragmentToDestManualSchedule(scheduleId)
                findNavController().navigate(action)
            },
            onLogCreateClick = { scheduleId, clientName, visitedDate ->
                val action = ScheduleFragmentDirections
                    .actionScheduleFragmentToDestVisitWrite(scheduleId, false)
                findNavController().navigate(action)
            },
            onLogViewClick = { scheduleId ->
                Log.d("onLogViewClick", "$scheduleId")
                val action = ScheduleFragmentDirections
                    .actionScheduleFragmentToDestDiaryDetail(scheduleId)
                findNavController().navigate(action)
            }
        )

        binding.scheduleRecyclerView.adapter = scheduleAdapter
    }

}

class DayViewContainer(view: View) : com.kizitonwose.calendar.view.ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.tv_calendar_day)
    lateinit var day: CalendarDay
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()