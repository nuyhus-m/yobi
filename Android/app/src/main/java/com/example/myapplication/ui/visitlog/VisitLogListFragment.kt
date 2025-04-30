package com.example.myapplication.ui.visitlog

import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentVisitLogListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VisitLogListFragment : BaseFragment<FragmentVisitLogListBinding>(
    FragmentVisitLogListBinding::bind,
    R.layout.fragment_visit_log_list
) {

}