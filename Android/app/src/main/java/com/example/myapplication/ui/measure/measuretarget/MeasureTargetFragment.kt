package com.example.myapplication.ui.measure.measuretarget

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasureTargetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureTargetFragment : BaseFragment<FragmentMeasureTargetBinding>(
    FragmentMeasureTargetBinding::bind,
    R.layout.fragment_measure_target
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
    }

    private fun setupSpinner() {
        val tempStrings = listOf("이서현", "이호정", "박진현")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            tempStrings
        )

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                // TODO 측정 대상 선택
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}