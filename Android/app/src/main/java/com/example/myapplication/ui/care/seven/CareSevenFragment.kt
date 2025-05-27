package com.example.myapplication.ui.care.seven

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val adapter = DailyMetricAdapter{
        viewModel.loadMore()
    }

    private val args : CareSevenFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchMetrics(args.clientId)

        binding.recyclerView.apply {
            adapter = this@CareSevenFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())

            // 스크롤 충돌 방지
            setHasFixedSize(true)
            itemAnimator = null

            // 리사이클러 스크롤 이벤트
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    }
                }
            })
        }
        viewModel.metrics.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)

            if (list.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.release()
    }
}