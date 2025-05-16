package com.example.myapplication.ui.visitlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.databinding.DialogDeleteBinding

class DeleteDairyDialog : DialogFragment() {
    private var _binding: DialogDeleteBinding? = null
    private val binding get() = _binding!!

    private val args: DeleteDairyDialogArgs by navArgs()

    companion object {
        private const val DELETE_RESULT_KEY = "delete_confirmed"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnNo.setOnClickListener { dismiss() }
        binding.btnYes.setOnClickListener {
            // 결과 전달: 현재 다이얼로그 → 이전 BackStackEntry
            findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    DELETE_RESULT_KEY,
                    args.scheduleId
                )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}