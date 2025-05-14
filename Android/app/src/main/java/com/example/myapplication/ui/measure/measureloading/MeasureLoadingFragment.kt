package com.example.myapplication.ui.measure.measureloading

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.data.dto.model.BloodPressureResult
import com.example.myapplication.data.dto.model.BodyCompositionResult
import com.example.myapplication.data.dto.model.HeartRateResult
import com.example.myapplication.data.dto.model.StressResult
import com.example.myapplication.data.dto.model.TemperatureResult
import com.example.myapplication.databinding.FragmentMeasureLoadingBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MeasureLoadingFragment : BaseFragment<FragmentMeasureLoadingBinding>(
    FragmentMeasureLoadingBinding::bind,
    R.layout.fragment_measure_loading
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
        setTitle()
        initView()
        startMeasure()
        observeMeasureResult()
    }

    private fun initButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setTitle() {
        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.client?.name)
    }

    private fun initView() {
        binding.ivCharacter.post {
            val animationDrawable = binding.ivCharacter.background as AnimationDrawable
            animationDrawable.start()
        }
    }

    private fun startMeasure() {
        fitrusViewModel.startMeasure()
    }

    private fun observeMeasureResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fitrusViewModel.measureResult.collect { result ->
                    when (result) {
                        is BodyCompositionResult -> TODO()
                        is BloodPressureResult -> TODO()
                        is HeartRateResult -> {

                        }

                        is StressResult -> {

                        }

                        is TemperatureResult -> {

                        }
                    }
                }
            }
        }
    }
}