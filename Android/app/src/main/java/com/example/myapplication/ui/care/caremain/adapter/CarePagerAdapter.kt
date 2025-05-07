package com.example.myapplication.ui.care.caremain.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.ui.care.daily.CareDailyFragment
import com.example.myapplication.ui.care.report.CareReportFragment
import com.example.myapplication.ui.care.seven.CareSevenFragment

class CarePagerAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val args = (fragment as? com.example.myapplication.ui.care.caremain.CareMainFragment)?.let {
            val name = it.requireArguments().getString("name", "")
            bundleOf("name" to name)
        } ?: bundleOf()

        return when (position){
            0 -> CareDailyFragment()
            1 -> CareSevenFragment()
            2 -> {
                CareReportFragment().apply {
                    arguments = args
                }
            }
            else -> throw IllegalArgumentException("없는 탭")
        }
    }
}