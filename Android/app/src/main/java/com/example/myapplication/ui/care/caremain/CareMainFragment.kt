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
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.myapplication.ui.care.caremain.viewmodel.CareMainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CareMainFragment : BaseFragment<FragmentCareMainBinding>(
    FragmentCareMainBinding::bind,
    R.layout.fragment_care_main
) {

    private val viewModel: CareMainViewModel by viewModels()
    private val args: CareMainFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchClientDetail(args.clientId)

        viewModel.clientDetail.observe(viewLifecycleOwner) { detail ->
            binding.tvName.text = detail.name
            binding.tvGender.text = when (detail.gender) {
                0 -> "남자"
                1 -> "여자"
                else -> "기타"
            }

            binding.tvBirth.text = detail.birth
            binding.tvHeight.text = "${detail.height}cm"
            binding.tvWeight.text = "${detail.weight}kg"
            binding.tvAddress.text = detail.address

            Glide.with(this)
                .load(detail.image)
                .transform(CenterCrop(), RoundedCorners(dpToPx(12)))
                .into(binding.ivProfile)
        }
        setupTabLayout()

    }

    private fun setupTabLayout() {
        val pagerAdapter = CarePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        val tabTitles = listOf("일일\n건강상태", "내\n건강추이", "주간\n보고서")

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val customView = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.custom_tab, null) as TextView
            customView.text = tabTitles[position]
            tab.customView = customView
        }.attach()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

}