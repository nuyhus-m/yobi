package com.example.myapplication.ui.care.caremain

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareMainBinding
import com.example.myapplication.ui.care.caremain.adapter.CarePagerAdapter
import com.example.myapplication.ui.care.caremain.viewmodel.CareMainViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "CareMainFragment"

@AndroidEntryPoint
class CareMainFragment : BaseFragment<FragmentCareMainBinding>(
    FragmentCareMainBinding::bind,
    R.layout.fragment_care_main
) {

    private val viewModel: CareMainViewModel by viewModels()
    private val args: CareMainFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearInitialUIValues()
        setupTabLayout()

        // 처음에는 스켈레톤 뷰 표시
        showSkeletonView(true)

        viewModel.fetchClientDetail(args.clientId)
        Log.d(TAG, "onViewCreated: ${args.clientId}")

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.clientDetail.observe(viewLifecycleOwner) { detail ->

            // 데이터가 있어도 최소 0.5초는 스켈레톤 유지 시키기
            binding.shimmerLayout.postDelayed({
                // 데이터 설정 및 실제 컨텐츠 표시
                binding.tvName.text = detail.name
                binding.tvGender.text = if (detail.gender == 0) "남자" else "여자"
                binding.tvBirth.text = detail.birth
                binding.tvHeight.text = "${detail.height}cm"
                binding.tvWeight.text = "${detail.weight}kg"
                binding.tvAddress.text = detail.address

                // 프로필 이미지가 있다면 로드
                detail.image?.let { imageUrl ->
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .transform(CenterCrop(), RoundedCorners(dpToPx(8)))
                        .into(binding.ivProfile)
                }

                // 스켈레톤 뷰 숨기기
                showSkeletonView(false)

            }, 500)
        }

    }

    private fun clearInitialUIValues() {
        binding.tvName.text = ""
        binding.tvGender.text = ""
        binding.tvBirth.text = ""
        binding.tvHeight.text = ""
        binding.tvWeight.text = ""
        binding.tvAddress.text = ""
    }

    private fun showSkeletonView(show: Boolean) {
        if (show) {
            // 스켈레톤 뷰 표시
            binding.contentLayout.visibility = View.INVISIBLE  // GONE 대신 INVISIBLE 사용 (레이아웃 깜빡임 방지)
            binding.shimmerLayout.visibility = View.VISIBLE
            binding.shimmerLayout.startShimmer()
        } else {
            // 실제 콘텐츠 표시
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE
        }
    }

    private fun setupTabLayout() {
        // 👉 처음에는 name 없이도 adapter 설정
        val pagerAdapter = CarePagerAdapter(this, args.clientId, "")
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = 3

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