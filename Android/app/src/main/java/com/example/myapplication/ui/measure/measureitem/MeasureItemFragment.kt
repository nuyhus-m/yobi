package com.example.myapplication.ui.measure.measureitem

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasureItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureItemFragment : BaseFragment<FragmentMeasureItemBinding>(
    FragmentMeasureItemBinding::bind,
    R.layout.fragment_measure_item
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}