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
import com.example.myapplication.ui.schedule.YearMonthPickerDialog
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
            showYearMonthPicker()
        }

        // 등록 버튼 클릭 시
        binding.btnRegister.setOnClickListener {
            showToast("등록 완료")
            findNavController().popBackStack()
        }
    }

    private fun showYearMonthPicker() {
        val dialog = YearMonthPickerDialog()
        dialog.setListener { year, month ->
            val formatted = String.format("%d-%02d", year, month)
            binding.etDate.setText(formatted)
            binding.tvDeleteNotice.text = "이전에 기록된 $formatted 스케줄은 모두 삭제됩니다."
            checkValid()
        }
        dialog.show(parentFragmentManager, "YEAR_MONTH_PICKER")
    }

    private fun checkValid() {
        val isPhotoSelected = selectedPhotoUri != null
        val isDateSelected = binding.etDate.text.toString().isNotEmpty()
        binding.btnRegister.isEnabled = isPhotoSelected && isDateSelected
    }
}