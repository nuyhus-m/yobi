package com.example.myapplication.ui.schedule

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentScheduleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleFragment : BaseFragment<FragmentScheduleBinding>(
    FragmentScheduleBinding::bind,
    R.layout.fragment_schedule
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tv.setOnClickListener {
            findNavController().navigate(R.id.dest_schedule_register_dialog)
        }
    }
}