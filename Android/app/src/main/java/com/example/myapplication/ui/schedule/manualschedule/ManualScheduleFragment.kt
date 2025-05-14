package com.example.myapplication.ui.schedule.manualschedule

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentManualScheduleBinding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.ui.schedule.DatePickerDialog
import com.example.myapplication.ui.schedule.TimePickerDialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class ManualScheduleFragment: BaseFragment<FragmentManualScheduleBinding>(
    FragmentManualScheduleBinding::bind,
    R.layout.fragment_manual_schedule
) {

    private val args: ManualScheduleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupScheduleSpinner()

        val scheduleId = args.scheduleId
        val isEditMode = scheduleId != -1

        if (isEditMode) {
            // 수정 모드
            binding.tvTitle.text = "일정 수정"
            binding.btnDelete.visibility = View.VISIBLE

            // TODO: scheduleId를 기반으로 ViewModel 또는 Repository에서 해당 데이터를 가져와서 세팅


        } else {
            // 등록 모드
            binding.tvTitle.text = "일정 등록"
            binding.btnDelete.visibility = View.GONE
        }

        // 버튼 클릭: 이전 화면으로
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etDate.setOnClickListener {
            val dialog = DatePickerDialog()
            dialog.onDateSelected = { selectedDate ->
                binding.etDate.setText(selectedDate.toString()) // 포맷 조정 가능
                checkValid()
            }
            dialog.show(parentFragmentManager, "DatePickerDialog")
        }

        // 등록 버튼 클릭 시
        binding.btnRegister.setOnClickListener {
            showToast("일정이 등록되었습니다.")
            findNavController().popBackStack()
        }

        binding.btnDelete.setOnClickListener{
            showToast("일정이 삭제되었습니다.")
            findNavController().popBackStack()
        }

        binding.tvStartTime.setOnClickListener {
            val dialog = TimePickerDialog()
            dialog.onTimeSelected = { time ->
                binding.tvStartTime.setText(formatTime(time))
                checkValid()
            }
            dialog.show(parentFragmentManager, "StartTimeDialog")
        }

        binding.tvEndTime.setOnClickListener {
            val dialog = TimePickerDialog()
            dialog.onTimeSelected = { time ->
                binding.tvEndTime.setText(formatTime(time))
                checkValid()
            }
            dialog.show(parentFragmentManager, "EndTimeDialog")
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

    private fun formatTime(time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("a h:mm") // 예: 오후 3:00
        return time.format(formatter)
    }

}