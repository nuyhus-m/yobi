package com.example.myapplication.ui.care.carelist

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareListBinding
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.care.carelist.adapter.CareListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CareListFragment : BaseFragment<FragmentCareListBinding>(
    FragmentCareListBinding::bind,
    R.layout.fragment_care_list
) {

    private val mainViewModel: MainViewModel by activityViewModels()

    private val adapter = CareListAdapter { selectedUser ->
        val action = CareListFragmentDirections
            .actionCareListFragmentToCareMainFragment(
                clientId = selectedUser.clientId
            )
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_line)?.let {
            divider.setDrawable(it)
        }
        binding.recyclerView.addItemDecoration(divider)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        mainViewModel.clientList.observe(viewLifecycleOwner) { clientList ->
            adapter.submitList(clientList)
        }
    }

}
