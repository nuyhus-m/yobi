package com.example.myapplication.ui.visitlog.visitloglist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentVisitLogListBinding
import com.example.myapplication.ui.visitlog.visitloglist.adapter.FilterAdapter
import com.example.myapplication.ui.visitlog.visitloglist.adapter.VisitLogAdapter
import com.example.myapplication.ui.visitlog.visitloglist.viewmodel.VisitLogViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VisitLogListFragment : BaseFragment<FragmentVisitLogListBinding>(
    FragmentVisitLogListBinding::bind,
    R.layout.fragment_visit_log_list
) {
    private val viewModel: VisitLogViewModel by viewModels()

    private val logAdapter = VisitLogAdapter { selectedLog ->
        val action = VisitLogListFragmentDirections
            .actionVisitLogListFragmentToDiaryDetailFragment(
                scheduleId = selectedLog.scheduleId
            )
        findNavController().navigate(action)
    }

    private val filterAdapter = FilterAdapter { selected ->
        viewModel.selectFilter(selected)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rvFilter.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            rvFilter.adapter = filterAdapter
            rvLog.layoutManager = LinearLayoutManager(context)
            rvLog.adapter = logAdapter
        }

        viewModel.filterItems.observe(viewLifecycleOwner) {
            filterAdapter.submitList(it)
        }

        viewModel.filteredLogs.observe(viewLifecycleOwner) { logs ->

            logAdapter.submitList(logs)

            // 일지 목록 부분에 아무것도 없을시, 아무 일지가 없습니다! 라는 text 써주기.
            binding.tvNoDiary.visibility = if (logs.isNullOrEmpty()) View.VISIBLE else View.GONE

        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.forceRefresh()
    }
}