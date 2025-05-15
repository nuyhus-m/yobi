package com.example.myapplication.ui.mypage.changePassword

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.data.dto.request.mypage.ChangePasswordRequest
import com.example.myapplication.databinding.FragmentChangePasswordBinding
import com.example.myapplication.ui.mypage.changePassword.viewmodel.ChangePasswordViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment: BaseFragment<FragmentChangePasswordBinding>(
    FragmentChangePasswordBinding::bind,
    R.layout.fragment_change_password
){
    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passwordPattern = Regex("^[a-zA-Z0-9@\$!%*#?&]{8,15}\$")

        binding.btnDone.setOnClickListener {
            val currentPw = binding.etCurrentPw.text.toString()
            val newPw = binding.etNewPw.text.toString()
            val confirmPw = binding.etConfirmPw.text.toString()

            if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                showToast("모든 항목을 입력해주세요.")
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                showToast("새 비밀번호가 일치하지 않습니다.")
                return@setOnClickListener
            }

            if (!passwordPattern.matches(newPw)) {
                showToast("비밀번호는 숫자, 영어, 특수문자(@\$!%*#?&)로 이루어진 8~15자여야 합니다.")
                return@setOnClickListener
            }

            val request = ChangePasswordRequest(currentPw, newPw)
            viewModel.changePassword(request)
        }

        viewModel.changePasswordSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                showToast("비밀번호가 변경되었습니다.")
                findNavController().popBackStack()
            } else {
                showToast("비밀번호 변경에 실패했습니다.")
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }
}