package com.example.myapplication.ui.schedule.manualschedule

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

        setupScheduleSpinner()

        // 버튼 클릭: 이전 화면으로
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

//        // 각 입력 항목 클릭 리스너 또는 변경 감지 등록
//        binding.tvSpinnerClient.setOnClickListener {
//            // TODO: 다이얼로그나 Spinner로 대상 선택 (임시 처리)
////            binding.tvSpinnerClient.settext("박진현") // 예시
////            checkValid()
//        }

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
//        val name = binding.tvSpinnerClient.text?.toString()?.isNotBlank() == true
        val date = binding.etDate.text?.toString()?.isNotBlank() == true
        val start = binding.tvStartTime.text?.isNotBlank() == true
        val end = binding.tvEndTime.text?.isNotBlank() == true

//        binding.btnRegister.isEnabled = name && date && start && end
        binding.btnRegister.isEnabled = date && start && end
    }

    private fun setupScheduleSpinner() {
        val tempStrings = listOf("이서현", "이호정", "박진현")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            tempStrings
        )

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.tvSpinnerClient.adapter = adapter

        binding.tvSpinnerClient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                // TODO 스케줄 등록 및 수정 대상 선택
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

}