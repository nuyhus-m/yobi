package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogDatePickerBinding
import com.example.myapplication.databinding.DialogYearMonthPickerBinding
import com.example.myapplication.ui.schedule.schedule.DayViewContainer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class DatePickerDialog: DialogFragment() {

    private var _binding: DialogDatePickerBinding? = null
    private val binding get() = _binding!!

    // 날짜 선택 결과 콜백
    var onDateSelected: ((LocalDate) -> Unit)? = null

    private var selectedDate = LocalDate.now()

    var currentMonth = YearMonth.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDialogSize()

        val calendarView = binding.cv
        val tvDate = binding.tvDate

        currentMonth = YearMonth.from(selectedDate)
        tvDate.text = formatDateYearMonth(currentMonth)

        val minMonth = YearMonth.of(2024, 1)
        val maxMonth = YearMonth.now().plusMonths(1)

        calendarView.setup(
            YearMonth.of(2024, 1),                      // 시작 월 (2024년 1월)
            YearMonth.now().plusMonths(1),             // 종료 월 (현재 기준 한 달 후)
            daysOfWeek().first()                        // 주 시작 요일 (일요일)
        )

        calendarView.scrollToDate(selectedDate)

        tvDate.text = formatDateYearMonth(currentMonth)

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
            }
        }

        binding.btnYes.setOnClickListener {
            onDateSelected?.invoke(selectedDate)
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnPrevious.setOnClickListener {
            if (currentMonth > minMonth) {
                currentMonth = currentMonth.minusMonths(1)
                binding.cv.scrollToMonth(currentMonth)
                binding.tvDate.text = formatDateYearMonth(currentMonth)
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentMonth < maxMonth) {
                currentMonth = currentMonth.plusMonths(1)
                binding.cv.scrollToMonth(currentMonth)
                binding.tvDate.text = formatDateYearMonth(currentMonth)
            }
        }

        binding.cv.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            binding.tvDate.text = formatDateYearMonth(currentMonth)
        }

    }

    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월")
        return date.format(formatter)
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.tv_calendar_day_picker)
        lateinit var day: CalendarDay
    }

    private fun formatDateYearMonth(yearMonth: YearMonth): String {
        return "${yearMonth.year}년 ${yearMonth.monthValue}월"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDialogSize() {
        val displayMetrics = resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels

        val params = dialog?.window?.attributes
        params?.width = (widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_white_radius_15)
    }
}