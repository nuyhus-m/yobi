package com.example.myapplication.ui.care.caremain

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareMainBinding
import com.example.myapplication.ui.care.caremain.adapter.CarePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import android.view.LayoutInflater

@AndroidEntryPoint
class CareMainFragment : BaseFragment<FragmentCareMainBinding>(
    FragmentCareMainBinding::bind,
    R.layout.fragment_care_main
) {

    private val args: CareMainFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvName.text = args.name
        binding.tvGender.text = args.gender
        binding.tvBirth.text = args.birth
        binding.ivProfile.setImageResource(args.image)

        val pagerAdapter = CarePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        val tabTitles = listOf("일일\n건강상태", "내\n건강추이", "주간\n보고서")

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val customView = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.custom_tab, null) as TextView
            customView.text = tabTitles[position]
            tab.customView = customView
        }.attach()
//
//        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
//            tab.text = when (position) {
//                0 -> "일일\n건강상태"
//                1 -> "내\n건강추이"
//                2 -> "주간\n보고서"
//                else -> ""
//            }
//        }.attach()
    }


}