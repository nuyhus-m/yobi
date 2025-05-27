package com.example.myapplication.ui.measure.measuretransition

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
import com.example.myapplication.databinding.FragmentMeasureTransitionBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MeasureTransitionFragment : BaseFragment<FragmentMeasureTransitionBinding>(
    FragmentMeasureTransitionBinding::bind,
    R.layout.fragment_measure_transition
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBackButton()
        setTitle()
        initNextButton()
        observeToastMessage()
    }

    private fun initBackButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setTitle() {
        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.client.name)
    }

    private fun initNextButton() {
        binding.btnNext.setOnClickListener {
            fitrusViewModel.setMeasureType(HealthDataType.BLOOD_PRESSURE)
            findNavController().navigate(R.id.dest_measure_guide)
        }
    }

    private fun observeToastMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fitrusViewModel.toastMessage.collect {
                    showToast(it)
                }
            }
        }
    }
}