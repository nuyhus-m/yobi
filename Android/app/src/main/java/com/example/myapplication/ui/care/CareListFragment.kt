package com.example.myapplication.ui.care

import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CareListFragment : BaseFragment<FragmentCareListBinding>(
    FragmentCareListBinding::bind,
    R.layout.fragment_care_list
) {

}