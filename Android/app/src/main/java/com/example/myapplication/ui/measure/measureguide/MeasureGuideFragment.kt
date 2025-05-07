package com.example.myapplication.ui.measure.measureguide

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasureGuideBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureGuideFragment : BaseFragment<FragmentMeasureGuideBinding>(
    FragmentMeasureGuideBinding::bind,
    R.layout.fragment_measure_guide
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}