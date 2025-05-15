package com.example.myapplication.ui.care.daily

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
    private val args: CareDailyFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchTodayData(args.clientId)

        viewModel.todayData.observe(viewLifecycleOwner) { data ->
            data?.let {
                // 체성분
                // 체지방량
                binding.tvFatRatio.text = it.bodyComposition?.bfp?.value.toString()
                binding.tvFatRatioLevel.text = it.bodyComposition?.bfp?.level.toString()

                // 기초대사량
                binding.tvBmr.text = (it.bodyComposition?.bmr?.value)?.toInt().toString()
                binding.tvBmrLevel.text = it.bodyComposition?.bmr?.level.toString()

                // 체내수분
                binding.tvBodyMoisture.text = it.bodyComposition?.ecf?.value.toString()
                binding.tvMoistureLevel.text = it.bodyComposition?.ecf?.level.toString()


                val stressName = it.stress?.stressLevel

                binding.tvStressLevel.text = if (stressName == null) {
                    "측정을 먼저해주세요!"
                } else {
                    "오늘 스트레스 등급은\n${stressName} 상태에요"
                }

                // 심박
                val bpmValue = it.heartRate?.bpm?.value?.toInt()?.toString() ?: "---"
                val bpmLevel = it.heartRate?.bpm?.level ?: "---"

                binding.tvHeartRate.text = bpmValue
                binding.tvHeartRateLevel.text = bpmLevel
                // 혈압
                binding.tvSystole.text = (it.bloodPressure?.sbp?.value)?.toInt().toString()
                binding.tvSystoleLevel.text = it.bloodPressure?.sbp?.level.toString()
                binding.tvDiastole.text = (it.bloodPressure?.dbp?.value)?.toInt().toString()
                binding.tvDiastoleLevel.text = it.bloodPressure?.dbp?.level.toString()

            }
        }

        binding.tvDetail.setOnClickListener {
            val action = CareDailyFragmentDirections
                .actionGlobalDailyDetailFragment(clientId = args.clientId)

            findNavController().navigate(action)
        }
    }
}