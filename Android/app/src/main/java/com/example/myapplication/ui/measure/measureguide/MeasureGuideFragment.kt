package com.example.myapplication.ui.measure.measureguide

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.base.HealthDataType
import com.example.myapplication.databinding.FragmentMeasureGuideBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MeasureGuideFragment : BaseFragment<FragmentMeasureGuideBinding>(
    FragmentMeasureGuideBinding::bind,
    R.layout.fragment_measure_guide
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle()
        initButtons()
        initViews()
        observeConnectState()
    }

    private fun setTitle() {
        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.clientName)
    }

    private fun initButtons() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.dest_measure_loading)
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

    private fun observeConnectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fitrusViewModel.isConnected.collect {
                    if (!it) {
                        findNavController().navigate(R.id.action_dest_measure_guide_to_dest_device_connect)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        fitrusViewModel.disconnectDevice()
    }
}