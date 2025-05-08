package com.example.myapplication.ui.measure.deviceconnect

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentDeviceConnectBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceConnectFragment : BaseFragment<FragmentDeviceConnectBinding>(
    FragmentDeviceConnectBinding::bind,
    R.layout.fragment_device_connect
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clBtn.setOnClickListener {
            findNavController().navigate(R.id.dest_bluetooth_guide)
        }
    }
}