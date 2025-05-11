package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogDatePickerBinding
import com.example.myapplication.databinding.DialogYearMonthPickerBinding
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

        tvDate.text = formatDateYearMonth(currentMonth)

        calendarView.setup(
            YearMonth.now().minusMonths(12),
            YearMonth.now().plusMonths(12),
            daysOfWeek().first() // 일요일 시작
        )
        calendarView.scrollToDate(selectedDate)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView

                textView.text = day.date.dayOfMonth.toString()

                // ✅ 선택된 날짜면 배경과 글자색 변경
                if (day.date == selectedDate) {
                    textView.setBackgroundResource(R.drawable.bg_purple_radius_12)
                    textView.setTextColor(requireContext().getColor(android.R.color.white))
                } else {
                    textView.background = null
                    textView.setTextColor(requireContext().getColor(R.color.black))
                }

                // ✅ 클릭 시 selectedDate 갱신 및 캘린더 갱신
                textView.setOnClickListener {
                    val oldDate = selectedDate
                    selectedDate = day.date
                    calendarView.notifyDateChanged(oldDate)
                    calendarView.notifyDateChanged(selectedDate)

                    binding.tvDate.text = formatDate(day.date) // 상단 텍스트 변경
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

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnPrevious.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            binding.cv.scrollToMonth(currentMonth)
            binding.tvDate.text = formatDateYearMonth(currentMonth)
        }

        binding.btnNext.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            binding.cv.scrollToMonth(currentMonth)
            binding.tvDate.text = formatDateYearMonth(currentMonth)
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