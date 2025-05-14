package com.example.myapplication.ui.care.daily

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareDailyBinding
import com.example.myapplication.ui.care.daily.viewmodel.CareDailyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CareDailyFragment : BaseFragment<FragmentCareDailyBinding>(
    FragmentCareDailyBinding::bind,
    R.layout.fragment_care_daily
) {
    private val viewModel: CareDailyViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clientId = 8
        viewModel.fetchTodayData(clientId)

        viewModel.todayData.observe(viewLifecycleOwner) { data ->
            data?.let {
                // 체성분
                // 체지방량
                binding.tvFatRatio.text = it.bodyComposition?.bfp?.value.toString()
                binding.tvFatRatioLevel.text = it.bodyComposition?.bfp?.level.toString()

                // 기초대사량
                binding.tvBmr.text = it.bodyComposition?.bmr?.value.toString()
                binding.tvBmrLevel.text = it.bodyComposition?.bmr?.level.toString()

                // 체내수분
                binding.tvBodyMoisture.text = it.bodyComposition?.ecf?.value.toString()
                binding.tvMoistureLevel.text = it.bodyComposition?.ecf?.level.toString()


                val stressName = it.stress?.stressLevel.toString()
                // 스트레스
                binding.tvStressLevel.text = "오늘 스트레스 등급은\\n${stressName} 상태에요"

                // 심박
                binding.tvHeartRate.text = it.heartRate?.bpm.toString()

                // 혈압
                binding.tvSystole.text = it.bloodPressure?.sbp.toString()
                binding.tvDiastole.text = it.bloodPressure?.dbp.toString()
            }
        }

        binding.tvDetail.setOnClickListener {
            findNavController().navigate(R.id.action_global_dailyDetailFragment)
        }
    }
}