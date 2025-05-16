package com.example.myapplication.ui.care.daily

import android.os.Bundle
import android.util.Log
import android.view.View                         // ← 누락되어 있던 import
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.data.dto.response.care.TodayResponse
import com.example.myapplication.databinding.FragmentCareDailyBinding
import com.example.myapplication.ui.care.daily.viewmodel.CareDailyViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CareDailyFragment"

@AndroidEntryPoint
class CareDailyFragment : BaseFragment<FragmentCareDailyBinding>(
    FragmentCareDailyBinding::bind,
    R.layout.fragment_care_daily
) {

    private val viewModel: CareDailyViewModel by viewModels()
    private val args: CareDailyFragmentArgs by navArgs()

    /** 숫자·등급·아이콘 전용 스켈레톤 쌍 */
    private lateinit var skeletonPairs: List<Pair<ShimmerFrameLayout, View>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 15개 ID 쌍 초기화
        skeletonPairs = listOf(
            binding.sflFatRatio to binding.tvFatRatio,
            binding.sflFatRatioLevel to binding.tvFatRatioLevel,
            binding.sflBmr to binding.tvBmr,
            binding.sflBmrLevel to binding.tvBmrLevel,
            binding.sflBodyMoisture to binding.tvBodyMoisture,
            binding.sflMoistureLevel to binding.tvMoistureLevel,
            binding.sflStressIcon to binding.ivStressIcon,
            binding.sflStressLevel to binding.tvStressLevel,
            binding.sflHeartRate to binding.tvHeartRate,
            binding.sflHeartRateLevel to binding.tvHeartRateLevel,
            binding.sflSystole to binding.tvSystole,
            binding.sflDiastole to binding.tvDiastole,
            binding.sflSystoleLevel to binding.tvSystoleLevel,
            binding.sflDiastoleLevel to binding.tvDiastoleLevel
        )

        showValueSkeleton(true)

        viewModel.fetchTodayData(args.clientId)
        Log.d(TAG, "clientId = ${args.clientId}")

        viewModel.todayData.observe(viewLifecycleOwner) { data ->
            data ?: return@observe
            Log.d(TAG, "todayData: $data")

            updateUIWithData(data)

            viewLifecycleOwner.lifecycleScope.launch {
                delay(500)
                showValueSkeleton(false)
            }
        }

        binding.tvDetail.setOnClickListener {
            findNavController().navigate(
                CareDailyFragmentDirections
                    .actionGlobalDailyDetailFragment(args.clientId)
            )
        }
    }

    /* -------- API 값 바인딩 -------- */
    private fun updateUIWithData(data: TodayResponse) {
        // 체성분
        binding.tvFatRatio.text = data.bodyComposition?.bfp?.value?.toString().orEmpty()
        binding.tvFatRatioLevel.text = data.bodyComposition?.bfp?.level.orEmpty()

        binding.tvBmr.text =
            data.bodyComposition?.bmr?.value?.toInt()?.toString().orEmpty()
        binding.tvBmrLevel.text = data.bodyComposition?.bmr?.level.orEmpty()

        binding.tvBodyMoisture.text = data.bodyComposition?.ecf?.value?.toString().orEmpty()
        binding.tvMoistureLevel.text = data.bodyComposition?.ecf?.level.orEmpty()

        // 스트레스
        binding.tvStressLevel.text =
            data.stress?.stressLevel?.let { "오늘 스트레스 등급은\n${it} 상태에요" }
                ?: "측정을 먼저해주세요!"

        // 심박
        binding.tvHeartRate.text =
            data.heartRate?.bpm?.value?.toInt()?.toString() ?: "---"
        binding.tvHeartRateLevel.text =
            data.heartRate?.bpm?.level ?: "---"

        // 혈압
        binding.tvSystole.text =
            data.bloodPressure?.sbp?.value?.toInt()?.toString().orEmpty()
        binding.tvDiastole.text =
            data.bloodPressure?.dbp?.value?.toInt()?.toString().orEmpty()
        binding.tvSystoleLevel.text = data.bloodPressure?.sbp?.level.orEmpty()
        binding.tvDiastoleLevel.text = data.bloodPressure?.dbp?.level.orEmpty()
    }

    /* -------- 숫자/등급/아이콘 스켈레톤 토글 -------- */
    private fun showValueSkeleton(show: Boolean) {
        skeletonPairs.forEach { (skeleton, realView) ->
            if (show) {
                skeleton.startShimmer()
                skeleton.visibility = View.VISIBLE
                realView.visibility = View.INVISIBLE
            } else {
                skeleton.stopShimmer()
                skeleton.visibility = View.GONE
                realView.visibility = View.VISIBLE
            }
        }
    }
}
