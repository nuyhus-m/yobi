package com.example.myapplication.ui.care.dailydetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentDailyDetailBinding
import com.example.myapplication.ui.care.dailydetail.viewmodel.DailyDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

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
                val bc = it.bodyComposition
                binding.tvBodyFat.text = "체지방률: ${bc?.bfp?.value ?: "-"}%"
                binding.tvMuscleMass.text = "근육량: ${bc?.smm?.value ?: "-"}kg"
                binding.tvBmr.text = "기초대사량: ${bc?.bmr?.value ?: "-"}kcal"
                binding.tvBodyWater.text = "체내 수분: ${bc?.ecf?.value ?: "-"}L"
                binding.tvProtein.text = "단백질량: ${bc?.protein?.value ?: "-"}kg"
                binding.tvMineral.text = "무기질량: ${bc?.mineral?.value ?: "-"}kg"
                binding.tvBodyAge.text = "신체나이: ${bc?.bodyAge ?: "-"}세"

                binding.tvTemperature.text = "체온: ${it.temperature?.temperature?.value ?: "-"}℃"
                binding.tvSystolic.text = "수축기: ${it.bloodPressure?.sbp?.value ?: "-"}"
                binding.tvDiastolic.text = "이완기: ${it.bloodPressure?.dbp?.value ?: "-"}"
                binding.tvHeartRate.text = "심박수: ${it.heartRate?.bpm?.value ?: "-"} BPM"
                binding.tvOxygen.text = "산소포화도: ${it.heartRate?.oxygen?.value ?: "-"}%"
                binding.tvStressIndex.text = "스트레스 지수: ${it.stress?.stressValue?.value ?: "-"}"
                binding.tvStressLevel.text = "스트레스 등급: ${it.stress?.stressLevel ?: "-"}"


            }
        }
    }
}
