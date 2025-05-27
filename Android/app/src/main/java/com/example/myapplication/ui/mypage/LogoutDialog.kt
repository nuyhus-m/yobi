package com.example.myapplication.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.data.local.SharedPreferencesUtil
import com.example.myapplication.databinding.DialogLogoutBinding
import com.example.myapplication.ui.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class LogoutDialog : DialogFragment() {

    private var _binding: DialogLogoutBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferencesUtil: SharedPreferencesUtil


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLogoutBinding.inflate(inflater, container, false)
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
            sharedPreferencesUtil.clearTokens()

            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

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