package com.example.myapplication.ui.care.caremain.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.ui.care.caremain.CareMainFragment
import com.example.myapplication.ui.care.caremain.inter.NameUpdateListener
import com.example.myapplication.ui.care.daily.CareDailyFragment
import com.example.myapplication.ui.care.report.CareReportFragment
import com.example.myapplication.ui.care.seven.CareSevenFragment

class CarePagerAdapter(
    private val fragment: Fragment,
    private val clientId: Int,
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val bundle = bundleOf(
            "clientId" to clientId,
        )

        val newFragment = when (position) {
            0 -> CareDailyFragment().apply { arguments = bundle }
            1 -> CareSevenFragment().apply { arguments = bundle }
            2 -> CareReportFragment().apply { arguments = bundle }
            else -> throw IllegalArgumentException("없는 탭")
        }

        // 프래그먼트가 NameUpdateListener를 구현한 경우, CareMainFragment에 등록
        if (newFragment is NameUpdateListener && fragment is CareMainFragment) {
            fragment.registerFragment(position, newFragment)
        }

        return newFragment
    }
}