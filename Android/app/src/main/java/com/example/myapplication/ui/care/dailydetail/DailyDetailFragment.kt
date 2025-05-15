package com.example.myapplication.ui.care.dailydetail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentDailyDetailBinding
import com.example.myapplication.ui.care.dailydetail.viewmodel.DailyDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "DailyDetailFragment"

@AndroidEntryPoint
class DailyDetailFragment : BaseFragment<FragmentDailyDetailBinding>(
    FragmentDailyDetailBinding::bind,
    R.layout.fragment_daily_detail
) {
    private val viewModel: DailyDetailViewModel by viewModels()
    private val args: DailyDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchTodayDetailData(args.clientId)

        viewModel.todayDetailData.observe(viewLifecycleOwner) { data ->
            data?.let {
                // 체성분
                val bc = it.bodyComposition

                with(binding) {
                    val levelMap = listOf(
                        ivRatioLevel to bc?.bfp?.level,
                        ivBodyFatLevel to bc?.bfm?.level,
                        ivMuscleLevel to bc?.smm?.level,
                        ivBmrLevel to bc?.bmr?.level,
                        ivBodyWaterLevel to bc?.ecf?.level,
                        ivProteinLevel to bc?.protein?.level,
                        ivMineralLevel to bc?.mineral?.level,
                        ivTemperatureLevel to it.temperature?.temperature?.level,
                        ivSystolicLevel to it.bloodPressure?.sbp?.level,
                        ivDiastolicLevel to it.bloodPressure?.dbp?.level,
                        ivHeartRateLevel to it.heartRate?.bpm?.level,
                        ivOxygenLevel to it.heartRate?.oxygen?.level,
                        ivStressIndexLevel to it.stress?.stressValue?.level,
                        ivStressLevelLevel to it.stress?.stressLevel
                    )

                    levelMap.forEach { (imageView, level) ->
                        val iconRes = getLevelIconResOrNull(level)
                        if (iconRes != null) {
                            imageView.setImageResource(iconRes)
                            imageView.visibility = View.VISIBLE
                        } else {
                            imageView.visibility = View.GONE
                        }
                    }

                    binding.tvBodyFatRatio.text = "체지방률: ${bc?.bfp?.value ?: "-"}%"
                    binding.tvBodyFat.text = "체지방량: ${bc?.bfm?.value ?: "-"}kg"
                    binding.tvMuscleMass.text = "근육량: ${bc?.smm?.value ?: "-"}kg"
                    binding.tvBmr.text = "기초대사량: ${bc?.bmr?.value ?: "-"}kcal"
                    binding.tvBodyWater.text = "체내 수분: ${bc?.ecf?.value ?: "-"}L"
                    binding.tvProtein.text = "단백질량: ${bc?.protein?.value ?: "-"}kg"
                    binding.tvMineral.text = "무기질량: ${bc?.mineral?.value ?: "-"}kg"
                    binding.tvBodyAge.text = "신체나이: ${bc?.bodyAge ?: "-"}세"

                    // 체온
                    binding.tvTemperature.text = "체온: ${it.temperature?.temperature?.value ?: "-"}℃"

                    // 혈압
                    binding.tvSystolic.text = "수축기: ${it.bloodPressure?.sbp?.value ?: "-"}"
                    binding.tvDiastolic.text = "이완기: ${it.bloodPressure?.dbp?.value ?: "-"}"

                    // 심박
                    binding.tvHeartRate.text = "심박수: ${it.heartRate?.bpm?.value ?: "-"} BPM"
                    binding.tvOxygen.text = "산소포화도: ${it.heartRate?.oxygen?.value ?: "-"}%"

                    // 스트레스
                    binding.tvStressIndex.text = "스트레스 지수: ${it.stress?.stressValue?.value ?: "-"}"
                    binding.tvStressLevel.text = "스트레스 등급: ${it.stress?.stressLevel ?: "-"}"

                }

            }
        }
    }

    private fun getLevelIconResOrNull(level: String?): Int? {
        return when (level) {
            "낮음" -> R.drawable.ic_low
            "보통" -> R.drawable.ic_middle
            "높음" -> R.drawable.ic_high
            else -> null  // null 또는 예외적인 값은 아이콘 없음
        }
    }


}
