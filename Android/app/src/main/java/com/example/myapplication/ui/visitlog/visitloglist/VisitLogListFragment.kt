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
    private val REFRESH_KEY = "refresh_logs"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 새로고침 플래그 관찰 설정
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(
                REFRESH_KEY
            )
            ?.observe(this) { shouldRefresh ->
                if (shouldRefresh == true) {
                    // 서버에서 최신 데이터 강제로 다시 로드
                    viewModel.forceRefresh()
                    // 플래그 초기화
                    findNavController().currentBackStackEntry?.savedStateHandle?.set(
                        REFRESH_KEY,
                        false
                    )
                }
            }
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

        viewModel.filteredLogs.observe(viewLifecycleOwner) {
            logAdapter.submitList(it)
        }
    }
}