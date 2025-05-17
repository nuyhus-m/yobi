package com.example.myapplication.ui.care.dailydetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.data.dto.response.care.TodayDetailResponse
import com.example.myapplication.databinding.FragmentDailyDetailBinding
import com.example.myapplication.ui.care.dailydetail.viewmodel.DailyDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        clearInitialUIValues()
        showSkeletonView(true)

        viewModel.fetchTodayDetailData(args.clientId)

        viewModel.todayDetailData.observe(viewLifecycleOwner) { data ->
            data ?: return@observe

            viewLifecycleOwner.lifecycleScope.launch {
                delay(500)

                bindBody(data)
                showSkeletonView(false)
            }
        }


        binding.ivBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun bindBody(it: TodayDetailResponse) = with(binding) {
        val bc = it.bodyComposition
        // ===== 레벨 아이콘 =====
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
            ivStressIndexLevel to it.stress?.stressValue,
            ivStressLevelLevel to it.stress?.stressLevel
        )
        levelMap.forEach { (img, lvl) ->
            img.setImageResource(getLevelIconResOrNull(lvl.toString()) ?: 0)
            img.visibility = if (lvl == null) View.GONE else View.VISIBLE
        }

        // ===== 값 바인딩 =====
        tvBodyFatRatio.text = "체지방률: ${bc?.bfp?.value ?: "-"}%"
        tvBodyFat.text = "체지방량: ${bc?.bfm?.value ?: "-"}kg"
        tvMuscleMass.text = "근육량: ${bc?.smm?.value ?: "-"}kg"
        tvBmr.text = "기초대사량: ${bc?.bmr?.value ?: "-"}kcal"
        tvBodyWater.text = "체내 수분: ${bc?.ecf?.value ?: "-"}L"
        tvProtein.text = "단백질량: ${bc?.protein?.value ?: "-"}kg"
        tvMineral.text = "무기질량: ${bc?.mineral?.value ?: "-"}kg"
        tvBodyAge.text = "신체나이: ${bc?.bodyAge ?: "-"}세"

        tvTemperature.text = "체온: ${it.temperature?.temperature?.value ?: "-"}℃"
        tvSystolic.text = "수축기: ${it.bloodPressure?.sbp?.value ?: "-"}"
        tvDiastolic.text = "이완기: ${it.bloodPressure?.dbp?.value ?: "-"}"
        tvHeartRate.text = "심박수: ${it.heartRate?.bpm?.value ?: "-"} BPM"
        tvOxygen.text = "산소포화도: ${it.heartRate?.oxygen?.value ?: "-"}%"
        tvStressIndex.text = "스트레스 지수: ${it.stress?.stressValue ?: "-"}"
        tvStressLevel.text = "스트레스 등급: ${it.stress?.stressLevel ?: "-"}"
    }

    private fun clearInitialUIValues() = with(binding) {
        tvBodyFatRatio.text = ""
        tvBodyFat.text = ""
        tvMuscleMass.text = ""
        tvBmr.text = ""
        tvBodyWater.text = ""
        tvProtein.text = ""
        tvMineral.text = ""
        tvBodyAge.text = ""
        tvTemperature.text = ""
        tvSystolic.text = ""
        tvDiastolic.text = ""
        tvHeartRate.text = ""
        tvOxygen.text = ""
        tvStressIndex.text = ""
        tvStressLevel.text = ""
    }

    private fun showSkeletonView(show: Boolean) = with(binding) {
        if (show) {
            clContentLayout.visibility = View.INVISIBLE
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
        } else {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            clContentLayout.visibility = View.VISIBLE
        }
    }

    private fun getLevelIconResOrNull(level: String?) = when (level) {
        "낮음" -> R.drawable.ic_low
        "보통" -> R.drawable.ic_middle
        "높음" -> R.drawable.ic_high
        else -> null
    }
}
