package com.example.myapplication.ui.mypage.mypage

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMyPageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind,
    R.layout.fragment_my_page
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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