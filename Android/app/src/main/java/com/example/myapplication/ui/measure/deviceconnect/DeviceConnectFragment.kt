package com.example.myapplication.ui.measure.deviceconnect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentDeviceConnectBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeviceConnectFragment : BaseFragment<FragmentDeviceConnectBinding>(
    FragmentDeviceConnectBinding::bind,
    R.layout.fragment_device_connect
) {

    private val activityViewModel by activityViewModels<FitrusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle()
        initButtons()
        observeConnectState()

        activityViewModel.tryConnectDevice()
    }

    private fun setTitle() {
        binding.tvTitle.text = getString(R.string.measure_title, activityViewModel.clientName)
    }

    private fun initButtons() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clBtn.setOnClickListener {
            findNavController().navigate(R.id.dest_bluetooth_guide)
        }
    }

    private fun observeConnectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.isConnected.collect {
                    if (it) {
                        findNavController().navigate(R.id.dest_measure_guide)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        activityViewModel.stopTryConnectDevice()
    }
}