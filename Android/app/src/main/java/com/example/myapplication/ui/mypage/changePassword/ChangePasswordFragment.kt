package com.example.myapplication.ui.mypage.changePassword

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,15}\$")

        binding.btnDone.isEnabled = false

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateInputs(passwordPattern)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etCurrentPw.addTextChangedListener(watcher)
        binding.etNewPw.addTextChangedListener(watcher)
        binding.etConfirmPw.addTextChangedListener(watcher)


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

    private fun validateInputs(pattern: Regex) {
        val currentPw = binding.etCurrentPw.text.toString()
        val newPw = binding.etNewPw.text.toString()
        val confirmPw = binding.etConfirmPw.text.toString()

        var valid = true

        if (currentPw.isEmpty()) {
            valid = false
        } else {
            binding.tilCurrentPw.error = null
        }

        if (newPw.isEmpty()) {
            valid = false
        } else if (!pattern.matches(newPw)) {
            binding.tilNewPw.error = "영문, 숫자, 특수문자 포함 8~15자로 입력해주세요."
            valid = false
        } else {
            binding.tilNewPw.error = null
        }

        if (confirmPw.isEmpty()) {
            valid = false
        } else if (newPw != confirmPw) {
            binding.tilConfirmPw1.error = "비밀번호가 일치하지 않습니다."
            valid = false
        } else {
            binding.tilConfirmPw1.error = null
        }

        binding.btnDone.isEnabled = valid
    }

}