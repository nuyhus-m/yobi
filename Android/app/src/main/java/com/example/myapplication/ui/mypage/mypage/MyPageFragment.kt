package com.example.myapplication.ui.mypage.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.data.dto.response.UserResponse
import com.example.myapplication.databinding.FragmentMyPageBinding
import com.example.myapplication.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind,
    R.layout.fragment_my_page
) {
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserInfo = mainViewModel.userInfo.value

        if (currentUserInfo == null) {
            showSkeletonUI(true)
            mainViewModel.loadUserInfo()
        } else {
            showSkeletonUI(false)
            updateUserInfo(currentUserInfo)
        }

        mainViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            updateUserInfo(user)

            if (currentUserInfo == null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(400L)
                    showSkeletonUI(false)
                }
            }
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.dest_change_password)
        }

        binding.btnPolicy.setOnClickListener {
            findNavController().navigate(R.id.dest_policy)
        }

        binding.btnLogout.setOnClickListener {
            findNavController().navigate(R.id.dest_logout_dialog)
        }

        binding.btnAppInfo.setOnClickListener {
            findNavController().navigate(R.id.dest_withdrawal_dialog)
        }


    }

    private fun showSkeletonUI(show: Boolean) = with(binding) {
        sflName.visibility = if (show) View.VISIBLE else View.GONE
        ll.visibility = if (show) View.INVISIBLE else View.VISIBLE

        sflIv.visibility = if (show) View.VISIBLE else View.GONE
        ivAvatar.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    private fun updateUserInfo(user: UserResponse) {
        binding.tvName.text = "${user.name} 님"
        Glide.with(this)
            .load(user.image)
            .placeholder(R.drawable.bg_oval_stroke_purple)
            .circleCrop()
            .into(binding.ivAvatar)
    }

}