package com.example.myapplication.ui.care.report

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareReportBinding
import com.example.myapplication.ui.care.report.adapter.CareReportAdapter
import com.example.myapplication.ui.care.report.viewmodel.CareReportViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CareReportFragment : BaseFragment<FragmentCareReportBinding>(
    FragmentCareReportBinding::bind,
    R.layout.fragment_care_report
) {
    private val viewModel: CareReportViewModel by viewModels()

    // Get name directly from arguments bundle instead of using navArgs()
    private val name: String
        get() = arguments?.getString("name", "") ?: ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CareReportAdapter { report ->
            // Create a bundle for navigation instead of using Directions
            val bundle = Bundle().apply {
                putString("name", name)
                putString("dateRange", report.rangeText)
            }

            findNavController().navigate(R.id.dest_report_detail_fragment, bundle)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.dates.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}