package com.example.myapplication.ui.schedule.manualschedule

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentManualScheduleBinding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.data.dto.request.schedule.ScheduleRequest
import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.ui.schedule.DatePickerDialog
import com.example.myapplication.ui.schedule.TimePickerDialog
import com.example.myapplication.ui.schedule.manualschedule.viewmodel.ManualScheduleViewModel
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.util.TimeUtils.toEpochMillis
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ManualScheduleFragment: BaseFragment<FragmentManualScheduleBinding>(
    FragmentManualScheduleBinding::bind,
    R.layout.fragment_manual_schedule
) {

    private val args: ManualScheduleFragmentArgs by navArgs()
    private val manualScheduleViewModel: ManualScheduleViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()


    private var selectedClientId: Int? = null
    private var selectedDate: LocalDate? = null
    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.fetchClients()
        setupScheduleSpinner()
        setupUI()
        setupListeners()

    }

    private fun setupUI() {
        val scheduleId = args.scheduleId
        val isEditMode = scheduleId != -1

        if (isEditMode) {
            binding.tvTitle.text = "일정 수정"
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnRegister.text = "일정 수정하기"

            // 수정 모드일 때 데이터 로드
            loadScheduleData(scheduleId)
        } else {
            binding.tvTitle.text = "일정 등록"
            binding.btnDelete.visibility = View.GONE
            binding.btnRegister.text = "일정 등록하기"
        }
    }

    private fun loadScheduleData(scheduleId: Int) {

    }

    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etDate.setOnClickListener {
            val dialog = DatePickerDialog()
            dialog.onDateSelected = { date ->
                selectedDate = date
                binding.etDate.setText(date.toString())
                checkValid()
            }
            dialog.show(parentFragmentManager, "DatePickerDialog")
        }

        binding.tvStartTime.setOnClickListener {
            val dialog = TimePickerDialog()
            dialog.onTimeSelected = { time ->
                startTime = time
                binding.tvStartTime.setText(formatTime(time))
                checkValid()
            }
            dialog.show(parentFragmentManager, "StartTimeDialog")
        }

        binding.tvEndTime.setOnClickListener {
            val dialog = TimePickerDialog()
            dialog.onTimeSelected = { time ->
                endTime = time
                binding.tvEndTime.setText(formatTime(time))
                checkValid()
            }
            dialog.show(parentFragmentManager, "EndTimeDialog")
        }

        binding.btnRegister.setOnClickListener {
            selectedClientId?.let { clientId ->
                val visitedDate = selectedDate?.toEpochMillis()
                val startAt = selectedDate?.let { date -> startTime?.let { time -> toEpochMillis(date, time) } }
                val endAt = selectedDate?.let { date -> endTime?.let { time -> toEpochMillis(date, time) } }

                if (visitedDate == null || startAt == null || endAt == null) {
                    showToast("날짜와 시간을 모두 선택해주세요.")
                    return@setOnClickListener
                }

                if (startAt >= endAt) {
                    showToast("시작 시간은 종료 시간보다 빨라야 합니다.")
                    return@setOnClickListener
                }

                val request = ScheduleRequest(
                    clientId = clientId,
                    visitedDate = visitedDate,
                    startAt = startAt,
                    endAt = endAt
                )

                manualScheduleViewModel.registerSchedule(request,
                    onSuccess = {
                        showToast("일정이 등록되었습니다.")
                        findNavController().popBackStack()
                    },
                    onError = {
                        showToast("일정 등록에 실패했습니다.")
                    }
                )
            } ?: showToast("클라이언트를 선택해주세요.")
        }

        binding.btnDelete.setOnClickListener{
            // 삭제 구현
            showToast("일정이 삭제되었습니다.")
            findNavController().popBackStack()
        }

    }

    private fun selectClientInSpinner(clientId: Int) {
        val adapter = binding.tvSpinnerClient.adapter
        for (i in 0 until adapter.count) {
            val client = adapter.getItem(i) as? ClientDetailResponse
            if (client?.clientId == clientId) {
                binding.tvSpinnerClient.setSelection(i)
                break
            }
        }
    }


    private fun checkValid() {
        val hasClient = selectedClientId != null
        val hasDate = binding.etDate.text?.toString()?.isNotBlank() == true
        val hasStart = binding.tvStartTime.text?.isNotBlank() == true
        val hasEnd = binding.tvEndTime.text?.isNotBlank() == true

        binding.btnRegister.isEnabled = hasClient && hasDate && hasStart && hasEnd
    }

    private fun setupScheduleSpinner() {
        mainViewModel.clientList.observe(viewLifecycleOwner) { clients ->
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.item_spinner,
                clients
            )
            adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
            binding.tvSpinnerClient.adapter = adapter

            binding.tvSpinnerClient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selected = parent.getItemAtPosition(position) as ClientDetailResponse
                    selectedClientId = selected.clientId
                    checkValid()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedClientId = null
                    checkValid()
                }
            }
        }
    }

    private fun formatTime(time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("a h:mm") // 예: 오후 3:00
        return time.format(formatter)
    }

}