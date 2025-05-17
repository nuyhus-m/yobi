package com.example.myapplication.ui.care.daily

import android.R.color.transparent
import android.os.Bundle
import android.util.Log
import android.view.View
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


            viewLifecycleOwner.lifecycleScope.launch {
                delay(500)
                showValueSkeleton(false)
                updateUIWithData(data)

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

        // 체성분 binding
        binding.apply {
            val bc = data.bodyComposition
            tvFatRatio.text = bc?.bfp?.value?.toString() ?: "---"

            if (bc?.bfp?.value != null) {
                tvFatRatioLevel.text = bc?.bfp?.level
            } else {
                tvFatRatioLevel.text = ""
                tvFatRatioLevel.setBackgroundColor(
                    resources.getColor(transparent, null)
                )
            }

            // 기초대사량
            tvBmr.text =
                bc?.bmr?.value?.toInt()?.toString() ?: "---"
            if (bc?.bmr?.value != null) {
                tvBmrLevel.text = bc?.bmr?.level
            } else {
                tvBmrLevel.text = ""
                tvBmrLevel.setBackgroundColor(
                    resources.getColor(transparent, null)
                )
            }

            // 체내수분
            tvBodyMoisture.text = bc?.ecf?.value?.toString() ?: "---"

            if (bc?.ecf?.value != null) {
                tvMoistureLevel.text = bc?.ecf?.level
            } else {
                tvMoistureLevel.text = ""
                tvMoistureLevel.setBackgroundColor(
                    resources.getColor(transparent, null)
                )
            }

        }
        // 그 외
        binding.apply {
            // 스트레스
            tvStressLevel.text =
                data.stress?.stressLevel?.let { level ->
                    // 아이콘 설정
                    when (level) {
                        "높음" -> ivStressIcon.setImageResource(R.drawable.ic_stress_high)
                        "보통" -> ivStressIcon.setImageResource(R.drawable.ic_stress_normal)
                        "낮음" -> ivStressIcon.setImageResource(R.drawable.ic_stress_low)
                        else -> ivStressIcon.visibility = View.INVISIBLE
                    }

                    "오늘 스트레스 등급은\n${level} 상태에요"
                } ?: run {

                    "측정을 먼저해주세요!"
                }


            // 심박
            tvHeartRate.text =
                data.heartRate?.bpm?.value?.toInt()?.toString() ?: "---"

            if (data.heartRate?.bpm?.value != null) {
                tvHeartRateLevel.text = data.heartRate?.bpm?.value?.toInt()?.toString()
            } else {
                tvHeartRateLevel.text = ""
                tvHeartRateLevel.setBackgroundColor(
                    resources.getColor(transparent, null)
                )
            }

            // 혈압 (수축기)
            tvSystole.text =
                data.bloodPressure?.sbp?.value?.toInt()?.toString() ?: "---"
            if (data.bloodPressure?.sbp?.value != null) {
                tvSystoleLevel.text = data.bloodPressure?.sbp?.level
            } else {
                tvSystoleLevel.text = ""
                tvSystoleLevel.setBackgroundColor(
                    resources.getColor(transparent, null)
                )
            }
            // 혈압 (이완기)
            tvDiastole.text =
                data.bloodPressure?.dbp?.value?.toInt()?.toString() ?: "---"

            if (data.bloodPressure?.dbp?.value != null) {
                tvDiastoleLevel.text = data.bloodPressure?.dbp?.level
            } else {
                tvDiastoleLevel.text = ""
                tvDiastoleLevel.setBackgroundColor(
                    resources.getColor(transparent, null)
                )
            }
        }

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
