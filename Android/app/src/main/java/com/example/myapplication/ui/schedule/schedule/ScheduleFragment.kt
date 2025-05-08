package com.example.myapplication.ui.schedule.schedule

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentScheduleBinding
import com.example.myapplication.ui.schedule.schedule.adapter.ScheduleAdapter
import com.example.myapplication.ui.schedule.schedule.viewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.TextStyle
import java.util.Locale

import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.YearMonth


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

        // 날짜 셀 바인더 설정 - typeMatch 오류 수정 (MonthDayBinder로 변경)
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()
                textView.background = null

                when {
                    data.date == selectedDate -> {
                        textView.setBackgroundResource(R.drawable.bg_purple_radius_12)
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    }
                    data.position.name == "MonthDate" -> {
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                    }
                    else -> {
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                    }
                }

                // 날짜 클릭 리스너 설정
                container.view.setOnClickListener {
                    if (data.position.name == "MonthDate") {
                        val oldDate = selectedDate
                        selectedDate = data.date
                        calendarView.notifyDateChanged(data.date)
                        oldDate?.let { calendarView.notifyDateChanged(it) }
                    }
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
            YearMonth.now().plusMonths(10),             // 종료 월
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
        scheduleAdapter = ScheduleAdapter(emptyList())
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
