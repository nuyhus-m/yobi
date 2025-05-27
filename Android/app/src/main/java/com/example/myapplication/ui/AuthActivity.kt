package com.example.myapplication.ui

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : BaseActivity<ActivityAuthBinding>(ActivityAuthBinding::inflate) {

    private val authViewModel: AuthViewModel by viewModels()

    private var backPressedTime: Long = 0
    private var toast: Toast? = null
    private val backPressInterval = 2000L // 2초

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (System.currentTimeMillis() - backPressedTime < backPressInterval) {
                        toast?.cancel()
                        isEnabled = false
                        finishAffinity()
                    } else {
                        backPressedTime = System.currentTimeMillis()
                        toast = Toast.makeText(
                            this@AuthActivity,
                            "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.",
                            Toast.LENGTH_SHORT
                        )
                        toast?.show()
                    }
                }
            }
        )

        binding.btnLogin.isEnabled = false
        setupInputValidationWatcher()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val animationDrawable = binding.ivCharacter.background as AnimationDrawable
                animationDrawable.start()
            }
        }

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "로그인 중"

            val employeeNumberText = binding.tilEmployNumber.editText?.text.toString()
            val password = binding.tilPassword.editText?.text.toString()

            authViewModel.login(employeeNumberText.toInt(), password)
        }


        authViewModel.loginSuccess.observe(this) { success ->
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "로그인하기"

            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                showToast("로그인에 실패했습니다. 다시 시도해주세요.")

            }

        }

    }

    private fun setupInputValidationWatcher() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.btnLogin.isEnabled = checkInputValid()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.tilEmployNumber.editText?.addTextChangedListener(watcher)
        binding.tilPassword.editText?.addTextChangedListener(watcher)
    }


    private fun checkInputValid(): Boolean {
        val employeeNumber = binding.tilEmployNumber.editText?.text.toString()
        val password = binding.tilPassword.editText?.text.toString()
        return employeeNumber.length == 6 && password.length >= 8
    }
}