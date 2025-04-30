package com.example.myapplication.ui.measurement

import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasurementTargetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasurementTargetFragment : BaseFragment<FragmentMeasurementTargetBinding>(
    FragmentMeasurementTargetBinding::bind,
    R.layout.fragment_measurement_target
) {

}