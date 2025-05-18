package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogScheduleRegisterBinding
import androidx.navigation.fragment.navArgs

class ScheduleRegisterDialog : DialogFragment() {

    private var _binding: DialogScheduleRegisterBinding? = null
    private val binding get() = _binding!!

    val args: ScheduleRegisterDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogScheduleRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDialogSize()

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnPhoto.setOnClickListener {
            findNavController().navigate(R.id.dest_photo_schedule)
            dismiss()
        }

        binding.btnText.setOnClickListener {
            val action = ScheduleRegisterDialogDirections
                .actionScheduleRegisterDialogToDestManualSchedule(
                    scheduleId = -1,
                    visitedDate = args.visitedDate
                )
            findNavController().navigate(action)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}