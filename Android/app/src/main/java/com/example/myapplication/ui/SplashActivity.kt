package com.example.myapplication.ui

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.local.SharedPreferencesUtil
import com.example.myapplication.databinding.ActivityAuthBinding
import com.example.myapplication.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvInMyHand.alpha = 0f
        binding.tvYo.alpha = 0f
        binding.tvYang.alpha = 0f
        binding.tvBi.alpha = 0f
        binding.tvSeo.alpha = 0f

        // 애니메이션 실행 후 텍스트 등장
        binding.ivCharacter.post {
            val animationDrawable = binding.ivCharacter.background as AnimationDrawable
            animationDrawable.start()

            binding.root.postDelayed({
                listOf(
                    binding.tvInMyHand,
                    binding.tvYo,
                    binding.tvYang,
                    binding.tvBi,
                    binding.tvSeo
                ).forEach { textView ->
                    textView.animate().alpha(1f).setDuration(400).start()
                }
            }, 2300)

            lifecycleScope.launch {
                delay(3000)
                checkAutoLogin()
            }

        }

    }

    @Inject
    lateinit var sharedPreferencesUtil: SharedPreferencesUtil

    private fun checkAutoLogin() {
        val accessToken = sharedPreferencesUtil.getAccessToken()
        val nextActivity = if (!accessToken.isNullOrBlank()) {
            Log.d("login-success", "accessToken: ${accessToken}")
            MainActivity::class.java
        } else {
            AuthActivity::class.java
        }
        startActivity(Intent(this, nextActivity))
        finish()
    }

}