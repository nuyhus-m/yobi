package com.example.myapplication.ui.schedule

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogYearMonthPickerBinding
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.util.Calendar


class YearMonthPickerDialog: DialogFragment() {

    private var _binding: DialogYearMonthPickerBinding? = null
    private val binding get() = _binding!!

    private var listener: ((Int, Int) -> Unit)? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogYearMonthPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDialogSize()

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        // 미래 제한: 한 달 후
        calendar.add(Calendar.MONTH, 1)
        val maxYear = calendar.get(Calendar.YEAR)
        val maxMonth = calendar.get(Calendar.MONTH) + 1

        val minYear = 2024
        val minMonth = 1

        // 설정
        with(binding.npYearPicker) {
            minValue = minYear
            maxValue = maxYear
            value = currentYear
        }

        with(binding.npMonthPicker) {
            minValue = 1
            maxValue = 12
            value = currentMonth
        }

        // 연도 변경 시 월 제한 동기화
        binding.npYearPicker.setOnValueChangedListener { _, _, newYear ->
            binding.npMonthPicker.apply {
                when (newYear) {
                    minYear -> {
                        minValue = minMonth
                        maxValue = if (maxYear == minYear) maxMonth else 12
                    }
                    maxYear -> {
                        minValue = 1
                        maxValue = maxMonth
                    }
                    else -> {
                        minValue = 1
                        maxValue = 12
                    }
                }

                // 현재 선택된 월이 범위 밖이면 조정
                if (value < minValue) value = minValue
                if (value > maxValue) value = maxValue
            }
        }


        binding.btnYes.setOnClickListener {
            val selectedYear = binding.npYearPicker.value
            val selectedMonth = binding.npMonthPicker.value

            val selectedCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val maxCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            if (selectedCal.after(maxCal)) {
                Toast.makeText(requireContext(), "한 달 이후의 날짜는 선택할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 유효한 날짜면 전달
            listener?.invoke(selectedYear, selectedMonth)
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }
    }

    fun setListener(listener: (Int, Int) -> Unit) {
        this.listener = listener
    }

    private fun setDialogSize() {
        val displayMetrics = resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels

        val params = dialog?.window?.attributes
        params?.width = (widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_white_radius_15)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}