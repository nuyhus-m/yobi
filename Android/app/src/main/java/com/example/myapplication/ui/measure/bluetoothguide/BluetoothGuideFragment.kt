package com.example.myapplication.ui.measure.bluetoothguide

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentBluetoothGuideBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BluetoothGuideFragment : BaseFragment<FragmentBluetoothGuideBinding>(
    FragmentBluetoothGuideBinding::bind,
    R.layout.fragment_bluetooth_guide
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}