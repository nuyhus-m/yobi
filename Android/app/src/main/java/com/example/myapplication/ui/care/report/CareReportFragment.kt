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

    private val clientId: Int
        get() = arguments?.getInt("clientId") ?: -1

    private val name: String
        get() = arguments?.getString("name", "") ?: ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CareReportAdapter { report ->
            val bundle = Bundle().apply {
                putString("name", name)
                putString("dateRange", convertMillisToRange(report.createdAt))
                putInt("reportId", report.reportId)

            }
            findNavController().navigate(R.id.dest_report_detail_fragment, bundle)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.fetchReports(clientId)

        viewModel.reports.observe(viewLifecycleOwner) { reports ->
            adapter.submitList(reports)
        }
    }

    private fun convertMillisToRange(millis: Long): String {
        val formatter = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.KOREA)
        val date = java.util.Date(millis)
        val start = formatter.format(date)
        val end = formatter.format(java.util.Date(millis + 6 * 24 * 60 * 60 * 1000)) // 6일 후
        return "$start - $end"
    }
}
