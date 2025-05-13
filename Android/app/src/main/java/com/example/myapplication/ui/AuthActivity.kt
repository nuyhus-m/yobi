package com.example.myapplication.ui

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity<ActivityAuthBinding>(ActivityAuthBinding::inflate) {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.ivCharacter.post {
            val animationDrawable = binding.ivCharacter.background as AnimationDrawable
            animationDrawable.start()
        }

        binding.btnLogin.setOnClickListener {
            val employeeNumberText = binding.tilEmployNumber.editText?.text.toString()
            val password = binding.tilPassword.editText?.text.toString()

            val idPattern = Regex("^\\d{6}$")
            val passwordPattern = Regex("^[a-zA-Z0-9@\$!%*#?&]{8,15}\$")

            var isValid = true

            if (!idPattern.matches(employeeNumberText)) {
                binding.tvIdError.text = "아이디는 숫자 6자리여야 합니다."
                isValid = false
            } else {
                binding.tvIdError.text = ""
            }

            if (!passwordPattern.matches(password)) {
                binding.tvPasswordError.text = "비밀번호는 숫자, 영어, 특수문자(@\$!%*#?&)로 이루어진 8~15자여야 합니다."
                isValid = false
            } else {
                binding.tvPasswordError.text = ""
            }

            if (isValid) {
                val employeeNumber = employeeNumberText.toInt()
                authViewModel.login(employeeNumber, password)
            }
        }


        authViewModel.loginSuccess.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                showToast("로그인에 실패했습니다. 다시 시도해주세요.")

            }

        }

    }
}