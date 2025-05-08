package com.example.myapplication.ui.schedule.photoschedule

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentPhotoScheduleBinding
import com.example.myapplication.R
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class PhotoScheduleFragment: BaseFragment<FragmentPhotoScheduleBinding>(
    FragmentPhotoScheduleBinding::bind,
    R.layout.fragment_photo_schedule
) {
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                selectedPhotoUri = it
                binding.ivSelectedPhoto.visibility = View.VISIBLE
                binding.llPickPhoto.visibility = View.INVISIBLE
                Glide.with(requireContext())
                    .load(it)
                    .into(binding.ivSelectedPhoto)
                checkValid()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.isEnabled = false

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // 날짜 EditText 클릭 시 날짜 선택 다이얼로그
        binding.etDate.setOnClickListener {
            showMonthPickerDialog()
        }

        // 등록 버튼 클릭 시
        binding.btnRegister.setOnClickListener {
            showToast("등록 완료")
            findNavController().popBackStack()
        }
    }

    private fun showMonthPickerDialog() {
        val today = LocalDate.now()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("일정 날짜 선택")
            .setSelection(today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()

        datePicker.addOnPositiveButtonClickListener { millis ->
            val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            val formatted = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            binding.etDate.setText(formatted)
            checkValid()
        }

        datePicker.show(parentFragmentManager, "MonthPicker")
    }

    private fun checkValid() {
        val isPhotoSelected = selectedPhotoUri != null
        binding.btnRegister.isEnabled = isPhotoSelected
    }
}