package com.example.myapplication.ui.measure.measureitem

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.base.HealthDataType
import com.example.myapplication.databinding.FragmentMeasureItemBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureItemFragment : BaseFragment<FragmentMeasureItemBinding>(
    FragmentMeasureItemBinding::bind,
    R.layout.fragment_measure_item
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.client.name)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvBodyComp.setOnClickListener {
            fitrusViewModel.setMeasureType(HealthDataType.BODY_COMPOSITION)
            findNavController().navigate(R.id.dest_device_connect)
        }

        binding.tvHeartRate.setOnClickListener {
            fitrusViewModel.setMeasureType(HealthDataType.HEART_RATE)
            findNavController().navigate(R.id.dest_device_connect)
        }

        binding.tvBloodPressure.setOnClickListener {
            fitrusViewModel.setMeasureType(HealthDataType.BLOOD_PRESSURE)
            findNavController().navigate(R.id.dest_device_connect)
        }

        binding.tvStress.setOnClickListener {
            fitrusViewModel.setMeasureType(HealthDataType.STRESS)
            findNavController().navigate(R.id.dest_device_connect)
        }

        binding.tvBodyTemp.setOnClickListener {
            fitrusViewModel.setMeasureType(HealthDataType.TEMPERATURE)
            findNavController().navigate(R.id.dest_device_connect)
        }
    }
}