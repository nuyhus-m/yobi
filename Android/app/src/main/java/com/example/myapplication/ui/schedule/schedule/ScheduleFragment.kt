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
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var scheduleAdapter: ScheduleAdapter

    private var currentMonth = YearMonth.now()
    private var selectedDate: LocalDate? = null

    private val minMonth = YearMonth.of(2024, 1)
    private val maxMonth = YearMonth.now().plusMonths(1)

    // 캘린더 설정 상수
    companion object {
        private val START_MONTH = YearMonth.of(2024, 1)
        private const val MAX_DOTS_PER_DAY = 10
        private const val DOTS_PER_ROW = 3
        private const val DOT_MARGIN = 1
        private const val DOT_SIZE = 4
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        mainViewModel.clientList.observe(viewLifecycleOwner) { clients ->
            scheduleViewModel.setClientColors(clients)
            binding.cv.notifyCalendarChanged() // 캘린더 리프레시
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("needRefreshSchedule")
            ?.observe(viewLifecycleOwner) { need ->
                if (need == true) {
                    // 플래그 삭제 (재진입 중복 호출 방지)
                    findNavController().currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("needRefreshSchedule")

                    // 실제 갱신
                    scheduleViewModel.reloadCurrentDate()
                }
            }

    }

    override fun onResume() {
        super.onResume()
        scheduleViewModel.selectDate(LocalDate.now())

        binding.cv.findFirstVisibleMonth()?.let { month ->
            val start = month.weekDays.first().first().date.toEpochMillis()
            val end = month.weekDays.last().last().date.toEpochMillis()

            scheduleViewModel.getPeriodSchedule(start, end)
        }
    }


    private fun setupObservers() {
        scheduleViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val oldDate = selectedDate
            selectedDate = date
            refreshDateAppearance(oldDate, date)
        }

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { list ->
            scheduleAdapter.submitList(list)
        }
    }

    private fun setupClickListeners() {
        // 이전 월 버튼 클릭 이벤트
        binding.btnPrevious.setOnClickListener {
            if(currentMonth > minMonth) {
                currentMonth = currentMonth.minusMonths(1)
                binding.cv.smoothScrollToMonth(currentMonth)
                updateMonthTitle(currentMonth)
            }
        }

        // 다음 월 버튼 클릭 이벤트
        binding.btnNext.setOnClickListener {
            if (currentMonth < maxMonth) {
                currentMonth = currentMonth.plusMonths(1)
                binding.cv.smoothScrollToMonth(currentMonth)
                updateMonthTitle(currentMonth)
            }
        }

        // 일정 추가 버튼 클릭 이벤트
        binding.btnScheduleAdd.setOnClickListener {
            findNavController().navigate(R.id.dest_schedule_register_dialog)
        }
    }


    private fun setupCalendar() {
        val calendarView = binding.cv

        // 월 제목 초기화
        updateMonthTitle(currentMonth)

        // 날짜 셀 바인더 설정
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                // 날짜 표시 스타일 설정
                updateDateAppearance(container, data)

                // 날짜 클릭 리스너 설정
                container.view.setOnClickListener {
                    scheduleViewModel.selectDate(data.date)
                }

                // 일정 도트 표시
                updateScheduleDots(container, data)
            }
        }

        calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            updateMonthTitle(currentMonth)

            val start = month.weekDays.first().first().date.toEpochMillis()
            val end = month.weekDays.last().last().date.toEpochMillis()
            scheduleViewModel.getPeriodSchedule(start, end)
        }

        // 캘린더 설정
        calendarView.setup(
            START_MONTH,                    // 시작 월
            YearMonth.now().plusMonths(1),  // 종료 월
            daysOfWeek().first()            // 주 시작 요일 (일요일)
        )

        // 현재 월로 스크롤
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
            data.date == selectedDate -> {
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
        // 날짜 변경 시 뷰 갱신
        if (oldDate != null) {
            binding.cv.notifyDateChanged(oldDate)
        }

        if (newDate != null) {
            binding.cv.notifyDateChanged(newDate)
        }

        // 월 경계에 있는 날짜인 경우 해당 월들도 갱신
        refreshMonthsIfNeeded(oldDate, newDate)
    }

    private fun refreshMonthsIfNeeded(oldDate: LocalDate?, newDate: LocalDate?) {
        val currentCalendarMonth = binding.cv.findFirstVisibleMonth()?.yearMonth ?: return

        // 현재 표시 중인 월 갱신
        binding.cv.notifyMonthChanged(currentCalendarMonth)

        // 이전/다음 월에 선택된 날짜가 있는 경우 해당 월도 갱신
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


    // 월 제목 업데이트 함수
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

// utils/DimensionUtils.kt 같은 파일에 추가해도 됨
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()