package com.example.myapplication.ui.care.carelist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareListBinding
import com.example.myapplication.ui.care.carelist.adapter.CareListAdapter
import com.example.myapplication.ui.care.carelist.viewmodel.CareListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CareListFragment : BaseFragment<FragmentCareListBinding>(
    FragmentCareListBinding::bind,
    R.layout.fragment_care_list
) {

    private val viewModel: CareListViewModel by viewModels()
    private val adapter = CareListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.careUserList.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
    }

}
