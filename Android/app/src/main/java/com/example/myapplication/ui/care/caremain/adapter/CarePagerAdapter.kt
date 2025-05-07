package com.example.myapplication.ui.care.caremain.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.ui.care.daily.CareDailyFragment
import com.example.myapplication.ui.care.report.CareReportFragment
import com.example.myapplication.ui.care.seven.CareSevenFragment

class CarePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> CareDailyFragment()
            1 -> CareSevenFragment()
            2 -> CareReportFragment()
            else -> throw IllegalArgumentException("없는 탭")
        }
    }
}