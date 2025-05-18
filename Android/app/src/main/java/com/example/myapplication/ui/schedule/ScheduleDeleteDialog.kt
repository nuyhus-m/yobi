package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogScheduleDeleteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleDeleteDialog : DialogFragment() {

    private var _binding: DialogScheduleDeleteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleDeleteViewModel by viewModels()
    private val args: ScheduleDeleteDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogScheduleDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDialogSize()

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {
            viewModel.deleteSchedule(
                args.scheduleId,
                onSuccess = {
                    Toast.makeText(requireContext(), "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle
                        ?.set("needRefreshSchedule", true)
                    findNavController().navigate(R.id.action_dest_schedule_delete_dialog_to_dest_schedule)
                },
                onError = {
                    Toast.makeText(requireContext(), "일정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            )
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