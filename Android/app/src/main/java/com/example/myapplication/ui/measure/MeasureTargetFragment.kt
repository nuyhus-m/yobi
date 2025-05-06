package com.example.myapplication.ui.measure

import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasureTargetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureTargetFragment : BaseFragment<FragmentMeasureTargetBinding>(
    FragmentMeasureTargetBinding::bind,
    R.layout.fragment_measure_target
) {

}