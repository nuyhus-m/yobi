package com.example.myapplication.ui.schedule.schedule


import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentScheduleBinding
import com.example.myapplication.ui.schedule.schedule.adapter.ScheduleAdapter
import com.example.myapplication.ui.schedule.schedule.viewmodel.ScheduleItem
import com.example.myapplication.ui.schedule.schedule.viewmodel.ScheduleViewModel
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
    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var scheduleAdapter: ScheduleAdapter

    private var currentMonth = YearMonth.now()
    private var selectedDate: LocalDate? = LocalDate.now() // 기본은 오늘

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeScheduleList()

        val calendarView = binding.cv

        // 현재 월 표시 업데이트
        updateMonthTitle(currentMonth)

        // 이전 월 버튼 클릭 이벤트
        binding.btnPrevious.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            calendarView.smoothScrollToMonth(currentMonth)
            updateMonthTitle(currentMonth)
        }

        // 다음 월 버튼 클릭 이벤트
        binding.btnNext.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            calendarView.smoothScrollToMonth(currentMonth)
            updateMonthTitle(currentMonth)
        }

        // 날짜 셀 바인더 설정
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()
                textView.background = null

                when {
                    data.date.equals(selectedDate) -> {
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

                // 날짜 클릭 리스너 설정
                container.view.setOnClickListener {
                    // 날짜 클릭 시 어떤 position이든 선택 가능하도록 수정
                    val oldDate = selectedDate
                    selectedDate = data.date

                    // 새로 선택된 날짜 갱신
                    calendarView.notifyDateChanged(data.date)

                    // 이전에 선택된 날짜 갱신
                    oldDate?.let { calendarView.notifyDateChanged(it) }

                    // 날짜가 변경될 때 현재 표시된 모든 달력에 대해 다시 갱신
                    val currentMonth = calendarView.findFirstVisibleMonth()?.yearMonth
                    val nextMonth = currentMonth?.plusMonths(1)
                    val prevMonth = currentMonth?.minusMonths(1)

                    // 현재 달, 다음 달, 이전 달에 속한 날짜들을 모두 체크하여 갱신 (겹치는 날짜 표시를 위해)
                    if (currentMonth != null) {
                        calendarView.notifyMonthChanged(currentMonth)
                    }
                    if (nextMonth != null && (selectedDate?.let { it.month == nextMonth.month } == true ||
                                oldDate?.let { it.month == nextMonth.month } == true)) {
                        calendarView.notifyMonthChanged(nextMonth)
                    }
                    if (prevMonth != null && (selectedDate?.let { it.month == prevMonth.month } == true ||
                                oldDate?.let { it.month == prevMonth.month } == true)) {
                        calendarView.notifyMonthChanged(prevMonth)
                    }
                }

                // 1. 도트 컨테이너 초기화
                val dotContainer = container.view.findViewById<LinearLayout>(R.id.dot_container)
                dotContainer.children.forEach { (it as LinearLayout).removeAllViews() }

                // 2. 날짜에 해당하는 클라이언트 ID 리스트 가져오기
                val clientIds = viewModel.dotMap[data.date] ?: emptyList()

                // 3. 도트 View 추가 (최대 10개, 3개씩 나눠서 row1~4에 배치)
                clientIds.take(10).forEachIndexed { index, clientId ->
                    val dot = AppCompatImageView(container.view.context).apply {
                        setImageResource(R.drawable.ic_schedule_dot)
                        val color =
                            Color.parseColor(viewModel.clientColorMap[clientId] ?: "#000000")
                        imageTintList = ColorStateList.valueOf(color)
                        layoutParams = LinearLayout.LayoutParams(4.dp, 4.dp).apply {
                            setMargins(1.dp, 0.dp, 1.dp, 0.dp)
                        }
                    }

                    val row = container.view.findViewById<LinearLayout>(
                        when (index / 3) {
                            0 -> R.id.row1
                            1 -> R.id.row2
                            2 -> R.id.row3
                            else -> R.id.row4
                        }
                    )
                    row.addView(dot)
                }


            }
        }

        // 월 스크롤 리스너 설정
        calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            updateMonthTitle(currentMonth)
        }

        // 캘린더 설정
        calendarView.setup(
            YearMonth.of(2024, 1),                      // 시작 월
            YearMonth.now().plusMonths(1),             // 종료 월
            daysOfWeek().first()                        // 주 시작 요일 (일요일)
        )

        // 현재 월로 스크롤
        calendarView.scrollToMonth(currentMonth)

        binding.btnScheduleAdd.setOnClickListener {
            findNavController().navigate(R.id.dest_schedule_register_dialog)
        }
    }

    // 월 제목 업데이트 함수
    private fun updateMonthTitle(yearMonth: YearMonth) {
        val year = yearMonth.year
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        binding.tvDate.text = "${year}년 ${month}"
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(
            emptyList<ScheduleItem>(),
            viewModel,
            onEditClick = { scheduleId ->
                val action = ScheduleFragmentDirections
                    .actionScheduleFragmentToDestManualSchedule(scheduleId.toLong())
                findNavController().navigate(action)
            },
            onLogCreateClick = { scheduleId ->
                findNavController().navigate(R.id.dest_visit_write_fragment)
            }
        )

        binding.scheduleRecyclerView.adapter = scheduleAdapter
    }

    private fun observeScheduleList() {
        viewModel.scheduleList.observe(viewLifecycleOwner) { list ->
            scheduleAdapter.submitList(list)
        }
    }

}

class DayViewContainer(view: View) : com.kizitonwose.calendar.view.ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.tv_calendar_day)
    lateinit var day: CalendarDay
}

// utils/DimensionUtils.kt 같은 파일에 추가해도 됨
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

