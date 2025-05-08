package com.example.myapplication.ui.schedule.manualschedule

import android.os.Bundle
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentManualScheduleBinding
import androidx.navigation.fragment.findNavController

class ManualScheduleFragment: BaseFragment<FragmentManualScheduleBinding>(
    FragmentManualScheduleBinding::bind,
    R.layout.fragment_manual_schedule
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 버튼 클릭: 이전 화면으로
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 각 입력 항목 클릭 리스너 또는 변경 감지 등록
        binding.tvWho.setOnClickListener {
            // TODO: 다이얼로그나 Spinner로 대상 선택 (임시 처리)
            binding.tvWho.setText("박진현") // 예시
            checkValid()
        }

        binding.etDate.setOnClickListener {
            // TODO: 날짜 선택 다이얼로그 띄우기
            binding.etDate.setText("2025-04-29") // 예시
            checkValid()
        }

        binding.tvStartTime.setOnClickListener {
            // TODO: 시간 선택 다이얼로그
            binding.tvStartTime.text = "오후 3:00"
            checkValid()
        }

        binding.tvEndTime.setOnClickListener {
            // TODO: 시간 선택 다이얼로그
            binding.tvEndTime.text = "오후 4:00"
            checkValid()
        }

        // 등록 버튼 클릭 시
        binding.btnRegister.setOnClickListener {
            showToast("일정이 등록되었습니다.")
            findNavController().popBackStack()
        }
    }

    private fun checkValid() {
        val name = binding.tvWho.text?.toString()?.isNotBlank() == true
        val date = binding.etDate.text?.toString()?.isNotBlank() == true
        val start = binding.tvStartTime.text?.isNotBlank() == true
        val end = binding.tvEndTime.text?.isNotBlank() == true

        binding.btnRegister.isEnabled = name && date && start && end
    }

}