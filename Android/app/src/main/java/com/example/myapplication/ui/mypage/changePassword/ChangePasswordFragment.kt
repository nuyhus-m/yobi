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

        // 기존 비밀번호
        if (currentPw.isEmpty()) {
            binding.tvCurrentPwError.text = "기존 비밀번호를 입력해주세요."
            binding.tvCurrentPwError.visibility = View.VISIBLE
            valid = false
        } else {
            binding.tvCurrentPwError.visibility = View.GONE
        }

        if (newPw.isEmpty()) {
            binding.tvNewPwError.text = "새 비밀번호를 입력해주세요."
            binding.tvNewPwError.visibility = View.VISIBLE
            valid = false
        } else if (!pattern.matches(newPw)) {
            binding.tvNewPwError.text = "영문, 숫자, 특수문자 포함 8~15자로 입력해주세요."
            binding.tvNewPwError.visibility = View.VISIBLE
            valid = false
        } else {
            binding.tvNewPwError.visibility = View.GONE
        }

        if (confirmPw.isEmpty()) {
            binding.tvConfirmPwError.text = "비밀번호 확인을 입력해주세요."
            binding.tvConfirmPwError.visibility = View.VISIBLE
            valid = false
        } else if (newPw != confirmPw) {
            binding.tvConfirmPwError.text = "비밀번호가 일치하지 않습니다."
            binding.tvConfirmPwError.visibility = View.VISIBLE
            valid = false
        } else {
            binding.tvConfirmPwError.visibility = View.GONE
        }

        binding.btnDone.isEnabled = valid
    }


}