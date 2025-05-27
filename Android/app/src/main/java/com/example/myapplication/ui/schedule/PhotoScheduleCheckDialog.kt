package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogPhotoScheduleCheckBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoScheduleCheckDialog(
    private val onConfirm: (Boolean) -> Unit
): DialogFragment() {
    private var _binding: DialogPhotoScheduleCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPhotoScheduleCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDialogSize()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnRegister.setOnClickListener {
            onConfirm(true)
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
}