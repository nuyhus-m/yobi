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

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        binding.npYearPicker.apply {
            minValue = currentYear - 10
            maxValue = currentYear + 10
            value = currentYear
        }

        binding.npMonthPicker.apply {
            minValue = 1
            maxValue = 12
            value = Calendar.getInstance().get(Calendar.MONTH) + 1
        }

        binding.btnYes.setOnClickListener {
            listener?.invoke(binding.npYearPicker.value, binding.npMonthPicker.value)
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setDialogSize() {
        val displayMetrics = resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels

        val params = dialog?.window?.attributes
        params?.width = (widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_white_radius_15)
    }

    fun setListener(listener: (Int, Int) -> Unit) {
        this.listener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}