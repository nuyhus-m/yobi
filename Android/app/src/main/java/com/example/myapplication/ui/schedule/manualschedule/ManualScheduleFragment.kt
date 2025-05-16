package com.example.myapplication.ui.schedule.manualschedule

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.data.dto.request.schedule.ScheduleRequest
import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.databinding.FragmentManualScheduleBinding
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.schedule.DatePickerDialog
import com.example.myapplication.ui.schedule.TimePickerDialog
import com.example.myapplication.ui.schedule.manualschedule.viewmodel.ManualScheduleViewModel
import com.example.myapplication.util.TimeUtils.toEpochMillis
import com.example.myapplication.util.TimeUtils.toLocalDate
import com.example.myapplication.util.TimeUtils.toLocalTime
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ManualScheduleFragment : BaseFragment<FragmentManualScheduleBinding>(
    FragmentManualScheduleBinding::bind,
    R.layout.fragment_manual_schedule
) {
    // Constants
    companion object {
        private const val INVALID_SCHEDULE_ID = -1
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("a h:mm")
    }

    // View Models & Arguments
    private val args: ManualScheduleFragmentArgs by navArgs()
    private val manualScheduleViewModel: ManualScheduleViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    // State Variables
    private var selectedClientId: Int? = null
    private var selectedDate: LocalDate? = null
    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null
    private val isEditMode: Boolean by lazy { args.scheduleId != INVALID_SCHEDULE_ID }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        setupListeners()
        setupObservers()

        mainViewModel.fetchClients()
        if (isEditMode) {
            loadScheduleData(args.scheduleId)
        }
    }

    private fun initUI() {
        with(binding) {
            tvTitle.text = if (isEditMode) "일정 수정" else "일정 등록"
            btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
            btnRegister.text = if (isEditMode) "일정 수정하기" else "일정 등록하기"
            btnRegister.isEnabled = false
        }
    }

    private fun setupObservers() {
        mainViewModel.clientList.observe(viewLifecycleOwner) { clients ->
            setupClientSpinner(clients)

            if (isEditMode && selectedClientId != null) {
                selectClientInSpinner(selectedClientId)
            }
        }
    }

    private fun setupClientSpinner(clients: List<ClientDetailResponse>) {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            clients
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }

        binding.tvSpinnerClient.apply {
            this.adapter = adapter
            onItemSelectedListener = createClientSelectionListener()
        }
    }

    private fun createClientSelectionListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            val selected = parent.getItemAtPosition(position) as ClientDetailResponse
            selectedClientId = selected.clientId
            validateForm()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            selectedClientId = null
            validateForm()
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnBack.setOnClickListener { findNavController().popBackStack() }

            etDate.setOnClickListener { showDatePicker() }
            tvStartTime.setOnClickListener { showTimePicker(isStartTime = true) }
            tvEndTime.setOnClickListener { showTimePicker(isStartTime = false) }

            btnRegister.setOnClickListener { handleScheduleRegistration() }
            btnDelete.setOnClickListener { handleScheduleDeletion() }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog().apply {
            onDateSelected = { date ->
                selectedDate = date
                binding.etDate.setText(date.toString())
                validateForm()
            }
        }.show(parentFragmentManager, "DatePickerDialog")
    }

    private fun showTimePicker(isStartTime: Boolean) {
        TimePickerDialog().apply {
            onTimeSelected = { time ->
                if (isStartTime) {
                    startTime = time
                    binding.tvStartTime.setText(formatTime(time))
                } else {
                    endTime = time
                    binding.tvEndTime.setText(formatTime(time))
                }
                validateForm()
            }
        }.show(parentFragmentManager, "TimePickerDialog")
    }

    private fun handleScheduleRegistration() {
        val clientId = selectedClientId ?: run {
            showToast("클라이언트를 선택해주세요.")
            return
        }

        val visitedDate = selectedDate?.toEpochMillis()
        val startAt = selectedDate?.let { date -> startTime?.let { time -> toEpochMillis(date, time) } }
        val endAt = selectedDate?.let { date -> endTime?.let { time -> toEpochMillis(date, time) } }

        if (visitedDate == null || startAt == null || endAt == null) {
            showToast("날짜와 시간을 모두 선택해주세요.")
            return
        }

        if (startAt >= endAt) {
            showToast("시작 시간은 종료 시간보다 빨라야 합니다.")
            return
        }

        val request = ScheduleRequest(
            clientId = clientId,
            visitedDate = visitedDate,
            startAt = startAt,
            endAt = endAt
        )

        if (isEditMode) {
            updateSchedule(args.scheduleId, request)
        } else {
            createSchedule(request)
        }
    }

    private fun createSchedule(request: ScheduleRequest) {
        manualScheduleViewModel.registerSchedule(
            request,
            onSuccess = {
                showToast("일정이 등록되었습니다.")
                findNavController().popBackStack()
            },
            onError = { code ->
                when (code) {
                    "400-10" -> showToast("해당 시간에 다른 일정이 이미 존재합니다.")
                    "400-11" -> showToast("해당 날짜에 해당 돌봄 대상이 이미 존재합니다")
                    else -> showToast("일정 등록에 실패했습니다.")
                }
            }
        )
    }

    private fun updateSchedule(scheduleId: Int, request: ScheduleRequest) {
        manualScheduleViewModel.editSchedule(
            scheduleId,
            request,
            onSuccess = {
                showToast("일정이 수정되었습니다.")
                findNavController().popBackStack()
            },
            onError = { code ->
                when (code) {
                    "400-10" -> showToast("해당 시간에 다른 일정이 이미 존재합니다.")
                    "400-11" -> showToast("해당 날짜에 해당 돌봄 대상이 이미 존재합니다")
                    else -> showToast("일정 수정에 실패했습니다.")
                }
            }
        )
    }

    private fun handleScheduleDeletion() {
        manualScheduleViewModel.deleteSchedule(
            args.scheduleId,
            onSuccess = {
                showToast("일정이 삭제되었습니다.")
                findNavController().popBackStack()
            },
            onError = {
                showToast("일정 삭제에 실패했습니다.")
            }
        )
    }

    private fun loadScheduleData(scheduleId: Int) {
        manualScheduleViewModel.getSchedule(
            scheduleId,
            onSuccess = { schedule ->
                selectedClientId = schedule.clientId
                selectedDate = schedule.visitedDate.toLocalDate()
                startTime = schedule.startAt.toLocalTime()
                endTime = schedule.endAt.toLocalTime()

                with(binding) {
                    etDate.setText(selectedDate.toString())
                    tvStartTime.setText(formatTime(startTime!!))
                    tvEndTime.setText(formatTime(endTime!!))
                }

                if (binding.tvSpinnerClient.adapter != null && binding.tvSpinnerClient.adapter.count > 0) {
                    selectClientInSpinner(selectedClientId)
                }
                validateForm()
            },
            onError = {
                showToast("일정 정보를 불러오지 못했습니다.")
                findNavController().popBackStack()
            }
        )
    }

    private fun selectClientInSpinner(clientId: Int?) {
        val adapter = binding.tvSpinnerClient.adapter
        for (i in 0 until adapter.count) {
            val client = adapter.getItem(i) as? ClientDetailResponse
            if (client?.clientId == clientId) {
                binding.tvSpinnerClient.setSelection(i)
                break
            }
        }
    }

    private fun validateForm() {
        val hasClient = selectedClientId != null
        val hasDate = binding.etDate.text?.toString()?.isNotBlank() == true
        val hasStart = binding.tvStartTime.text?.isNotBlank() == true
        val hasEnd = binding.tvEndTime.text?.isNotBlank() == true

        binding.btnRegister.isEnabled = hasClient && hasDate && hasStart && hasEnd
    }

    private fun formatTime(time: LocalTime): String {
        return time.format(TIME_FORMATTER)
    }
}