package com.example.myapplication.ui.measure.measureresult

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasureResultBinding
import com.example.myapplication.databinding.LayoutResultOneBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureResultFragment : BaseFragment<FragmentMeasureResultBinding>(
    FragmentMeasureResultBinding::bind,
    R.layout.fragment_measure_result
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvMeasureItem.text = getString(R.string.temperature)
        setBodyTemperature()
    }

    private fun setBodyTemperature() {
        val viewStub = binding.vsResultOne.inflate()
        val resultBinding = LayoutResultOneBinding.bind(viewStub)

        val itemOneBinding = resultBinding.include1

        itemOneBinding.tvLabel.text = getString(R.string.temp)
        itemOneBinding.tvValue.text = "36.5"
        itemOneBinding.tvUnit.text = getString(R.string.unit_celsius)
        itemOneBinding.tvGrade.text = getString(R.string.high)
        itemOneBinding.tvGrade.setBackgroundResource(R.drawable.bg_red_sub_radius_4)
        itemOneBinding.tvGrade.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
    }
}