package com.example.myapplication.ui.care.caremain

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareMainBinding
import com.example.myapplication.ui.care.caremain.adapter.CarePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

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

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "일일\n건강상태"
                1 -> "내\n건강추이"
                2 -> "주간\n보고서"
                else -> ""
            }
        }.attach()
    }


}