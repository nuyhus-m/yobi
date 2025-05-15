package com.example.myapplication.ui.care.caremain.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.ui.care.daily.CareDailyFragment
import com.example.myapplication.ui.care.report.CareReportFragment
import com.example.myapplication.ui.care.seven.CareSevenFragment

class CarePagerAdapter(
    private val fragment: Fragment,
    private val clientId: Int,
    private val name: String

) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val bundle = bundleOf(
            "clientId" to clientId,
            "name" to name
        )

        return when (position) {
            0 -> CareDailyFragment().apply { arguments = bundle }
            1 -> CareSevenFragment().apply { arguments = bundle }
            2 -> CareReportFragment().apply { arguments = bundle }
            else -> throw IllegalArgumentException("없는 탭")
        }
    }
}
