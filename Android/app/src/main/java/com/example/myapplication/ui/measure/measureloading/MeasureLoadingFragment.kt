package com.example.myapplication.ui.measure.measureloading

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasureLoadingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureLoadingFragment : BaseFragment<FragmentMeasureLoadingBinding>(
    FragmentMeasureLoadingBinding::bind,
    R.layout.fragment_measure_loading
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
    }

    private fun initButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}