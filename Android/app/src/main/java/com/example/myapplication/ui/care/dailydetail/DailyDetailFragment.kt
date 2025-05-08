package com.example.myapplication.ui.care.dailydetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.apply {
                tvBodyFat.text = "체지방률: ${state.bodyFat}"
                tvMuscleMass.text = "근육량: ${state.muscleMass}"
                tvBmr.text = "기초대사량: ${state.bmr}"
                tvBodyWater.text = "체내 수분: ${state.bodyWater}"
                tvProtein.text = "단백질량: ${state.protein}"
                tvMineral.text = "무기질량: ${state.mineral}"
                tvBodyAge.text = "신체나이: ${state.bodyAge}"

                tvTemperature.text = "체온: ${state.temperature}°C"

                tvSystolic.text = "수축기: ${state.systolic}mmHg"
                tvDiastolic.text = "이완기: ${state.diastolic}mmHg"

                tvHeartRate.text = "심박수: ${state.heartRate}bpm"
                tvOxygen.text = "산소포화도: ${state.oxygen}%"

                tvStressIndex.text = "스트레스 지수: ${state.stressIndex}"
                tvStressLevel.text = "스트레스 등급: ${state.stressLevel}"
            }
        }
    }


}