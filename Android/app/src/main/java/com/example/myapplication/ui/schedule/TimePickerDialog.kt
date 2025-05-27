package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DialogTimePickerBinding
import java.time.LocalTime
import android.view.View
import android.view.WindowManager
import android.widget.NumberPicker
import com.example.myapplication.R
import java.util.Locale
import kotlin.math.ceil

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

        setDialogSize()

        val hourPicker = binding.npStartPicker
        val minutePicker = binding.npMinutePicker

        // 시간 설정 (오전/오후 12시간제로 설정할 수도 있음)
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.wrapSelectorWheel = true

        val values = Array(12) { i -> String.format(Locale.getDefault(), "%02d", i * 5) }

        minutePicker.minValue = 0
        minutePicker.maxValue = 11
        minutePicker.displayedValues = values

        val now = LocalTime.now()
        hourPicker.value = now.hour
        val adjustedMinuteIndex = ceil(now.minute / 5.0).toInt()
        minutePicker.value = adjustedMinuteIndex

        binding.btnYes.setOnClickListener {
            val selectedHour = hourPicker.value
            val selectedMinute = minutePicker.value * 5
            onTimeSelected?.invoke(LocalTime.of(selectedHour, selectedMinute))
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }
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