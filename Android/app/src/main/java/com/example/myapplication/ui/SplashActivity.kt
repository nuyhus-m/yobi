package com.example.myapplication.ui

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import com.example.myapplication.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 텍스트 먼저 안 보이게
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
        }

        // (선택) 메인 화면으로 전환할 경우
        // binding.root.postDelayed({
        //     startActivity(Intent(this, MainActivity::class.java))
        //     finish()
        // }, 2000)

    }
}