package com.example.myapplication.ui.care.seven

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareSevenBinding
import com.example.myapplication.ui.care.seven.adapter.DailyMetricAdapter
import com.example.myapplication.ui.care.seven.viewmodel.CareSevenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CareSevenFragment : BaseFragment<FragmentCareSevenBinding>(
    FragmentCareSevenBinding::bind,
    R.layout.fragment_care_seven
) {

    private val viewModel: CareSevenViewModel by viewModels()
    private val adapter = DailyMetricAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.metrics.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
}