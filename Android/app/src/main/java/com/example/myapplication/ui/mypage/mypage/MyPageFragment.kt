package com.example.myapplication.ui.mypage.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMyPageBinding
import com.example.myapplication.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind,
    R.layout.fragment_my_page
) {
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.loadUserInfo()

        mainViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            binding.tvName.text = "${user.name} 님"
            Glide.with(this)
                .load(user.image)
                .placeholder(R.drawable.bg_oval_stroke_purple)
                .circleCrop()
                .into(binding.ivAvatar)
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

        binding.btnSignout.setOnClickListener {
            findNavController().navigate(R.id.dest_withdrawal_dialog)
        }


    }

}