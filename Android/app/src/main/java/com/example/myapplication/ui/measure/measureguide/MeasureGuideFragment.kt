package com.example.myapplication.ui.measure.measureguide

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.base.HealthDataType
import com.example.myapplication.databinding.FragmentMeasureGuideBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureGuideFragment : BaseFragment<FragmentMeasureGuideBinding>(
    FragmentMeasureGuideBinding::bind,
    R.layout.fragment_measure_guide
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle()
        initButton()
        initViews()
    }

    private fun setTitle() {
        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.clientName)
    }

    private fun initButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initViews() {
        when (fitrusViewModel.measureType) {
            HealthDataType.BODY_COMPOSITION -> {
                binding.groupGrip.visibility = View.VISIBLE
                binding.tvGuide.text = getString(R.string.measure_grip_guide)
            }

            HealthDataType.HEART_RATE, HealthDataType.BLOOD_PRESSURE, HealthDataType.STRESS -> {
                binding.ivFinger.visibility = View.VISIBLE
                binding.tvGuide.text = getString(R.string.measure_finger_touch_guide)
            }

            HealthDataType.TEMPERATURE -> {
                binding.ivForehead.visibility = View.VISIBLE
                binding.tvGuide.text = getString(R.string.measure_forehead_touch_guide)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        fitrusViewModel.disconnectDevice()
    }
}