package com.example.myapplication.ui.care.caremain

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCareMainBinding
import com.example.myapplication.ui.care.caremain.adapter.CarePagerAdapter
import com.example.myapplication.ui.care.caremain.inter.NameUpdateListener
import com.example.myapplication.ui.care.caremain.viewmodel.CareMainViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CareMainFragment"

@AndroidEntryPoint
class CareMainFragment : BaseFragment<FragmentCareMainBinding>(
    FragmentCareMainBinding::bind,
    R.layout.fragment_care_main
) {

    private val viewModel: CareMainViewModel by viewModels()
    private val args: CareMainFragmentArgs by navArgs()
    private lateinit var pagerAdapter: CarePagerAdapter

    // 현재 어댑터를 통해 생성된 프래그먼트들
    private val pageFragments = mutableMapOf<Int, NameUpdateListener?>()

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

            binding.shimmerLayout.post {
                with(binding) {
                    tvName.text = detail.name
                    tvGender.text = if (detail.gender == 0) "남자" else "여자"
                    tvBirth.text = detail.birth
                    tvHeight.text = "${detail.height}cm"
                    tvWeight.text = "${detail.weight}kg"
                    tvAddress.text = detail.address

                    detail.image?.let { url ->
                        Glide.with(requireContext())
                            .load(url)
                            .transform(CenterCrop(), RoundedCorners(dpToPx(8)))
                            .into(ivProfile)
                    }
                }

                updateFragmentNames(detail.name) // ★ 이름 전파

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    showSkeletonView(false)
                }
            }
        }
    }

    // 이름 정보를 프래그먼트에 전달
    private fun updateFragmentNames(name: String) {
        // ★ 1. 자신(BackStackEntry)에 이름 저장 → 하위 화면 어디서든 꺼내 쓸 수 있음
        findNavController().currentBackStackEntry
            ?.savedStateHandle?.set("clientName", name)

        // 2. 이미 만들어진 자식 프래그먼트들(UI 갱신용)
        pageFragments.values.forEach { it?.onNameUpdated(name) }
    }


    // 페이저 어댑터에서 프래그먼트 참조를 등록할 수 있는 콜백
    fun registerFragment(position: Int, fragment: NameUpdateListener?) {
        pageFragments[position] = fragment
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
        // 탭 설정 및 어댑터 초기화 (this 참조 전달로 콜백 연결)
        pagerAdapter = CarePagerAdapter(this, args.clientId)
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