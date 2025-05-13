package com.example.myapplication.ui.measure.measureguide

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
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

        initButton()
    }

    private fun initButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onStop() {
        super.onStop()
        fitrusViewModel.disconnectDevice()
    }
}