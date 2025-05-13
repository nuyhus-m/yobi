package com.example.myapplication.ui.measure.measuretarget

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.base.HealthDataType
import com.example.myapplication.data.dto.response.ClientResponse
import com.example.myapplication.databinding.FragmentMeasureTargetBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "MeasureTargetFragment"

@AndroidEntryPoint
class MeasureTargetFragment : BaseFragment<FragmentMeasureTargetBinding>(
    FragmentMeasureTargetBinding::bind,
    R.layout.fragment_measure_target
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()
    private var selectedClientId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSpinner()
        initNextButton()
        observeMeasureStatus()
    }

    private fun initSpinner() {
        val tempClientList = listOf(
            ClientResponse(1, "김할아버지", "1960", 0, 170f, 60f, "", ""),
            ClientResponse(1, "김할아버지", "1960", 0, 170f, 60f, "", "")
        )

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            tempClientList
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
                val selected = parent.getItemAtPosition(position) as ClientResponse
                selectedClientId = selected.clientId
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun initNextButton() {
        binding.btnNext.setOnClickListener {
            fitrusViewModel.getClientDetail(selectedClientId)
        }
    }

    private fun observeMeasureStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fitrusViewModel.isInfoSuccess.collectLatest {
                    Log.d(TAG, "observeMeasureStatus: $it")
                    if (fitrusViewModel.isMeasured) {
                        findNavController().navigate(R.id.dest_measure_item)
                    } else {
                        fitrusViewModel.setMeasureType(HealthDataType.BODY_COMPOSITION)
                        findNavController().navigate(R.id.dest_device_connect)
                    }
                }
            }
        }
    }
}