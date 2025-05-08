package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DialogTimePickerBinding
import java.time.LocalTime
import android.view.View
import android.widget.NumberPicker
import com.example.myapplication.R

class TimePickerDialog: DialogFragment() {
    private var _binding: DialogTimePickerBinding? = null
    private val binding get() = _binding!!

    // 시간 선택 콜백
    var onTimeSelected: ((LocalTime) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hourPicker = binding.npStartPicker
        val minutePicker = binding.npMinutePicker

        // 시간 설정 (오전/오후 12시간제로 설정할 수도 있음)
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.wrapSelectorWheel = true

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.wrapSelectorWheel = true

        // 현재 시간으로 초기화
        val now = LocalTime.now()
        hourPicker.value = now.hour
        minutePicker.value = now.minute

        binding.btnYes.setOnClickListener {
            val selectedHour = hourPicker.value
            val selectedMinute = minutePicker.value
            onTimeSelected?.invoke(LocalTime.of(selectedHour, selectedMinute))
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}