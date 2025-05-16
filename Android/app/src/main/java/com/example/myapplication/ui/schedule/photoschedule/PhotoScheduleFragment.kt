package com.example.myapplication.ui.schedule.photoschedule

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentPhotoScheduleBinding
import com.example.myapplication.R
import com.example.myapplication.ui.schedule.YearMonthPickerDialog
import com.example.myapplication.ui.schedule.photoschedule.viewmodel.PhotoScheduleViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class PhotoScheduleFragment: BaseFragment<FragmentPhotoScheduleBinding>(
    FragmentPhotoScheduleBinding::bind,
    R.layout.fragment_photo_schedule
) {
    private val photoScheduleViewModel: PhotoScheduleViewModel by viewModels()
    private var selectedPhotoUri: Uri? = null
    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null

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

        binding.etDate.setOnClickListener {
            showYearMonthPicker()
        }

        binding.btnRegister.setOnClickListener {
            registerSchedule()
        }

        observeViewModel()
    }

    private fun showYearMonthPicker() {
        val dialog = YearMonthPickerDialog()
        dialog.setListener { year, month ->
            selectedYear = year
            selectedMonth = month
            val formatted = String.format("%d-%02d", year, month)
            binding.etDate.setText(formatted)
            binding.tvDeleteNotice.text = "이전에 기록된 $formatted 스케줄은 모두 삭제됩니다."
            checkValid()
        }
        dialog.show(parentFragmentManager, "YEAR_MONTH_PICKER")
    }

    private fun checkValid() {
        val isPhotoSelected = selectedPhotoUri != null
        val isDateSelected = selectedYear != null && selectedMonth != null
        binding.btnRegister.isEnabled = isPhotoSelected && isDateSelected
    }

    private fun registerSchedule() {
        if (photoScheduleViewModel.isLoading.value == true) return

        val uri = selectedPhotoUri ?: return
        val year = selectedYear ?: return
        val month = selectedMonth ?: return

        val file = uriToFile(uri) ?: run {
            showToast("파일을 불러올 수 없습니다.")
            return
        }

        photoScheduleViewModel.registerPhotoSchedule(file, year, month)
    }

    private fun observeViewModel() {
        photoScheduleViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnRegister.isEnabled = !isLoading
            binding.btnRegister.text = if (isLoading) "처리 중" else "일정 등록하기"
        }

        photoScheduleViewModel.ocrResult.observe(viewLifecycleOwner) { result ->
            val success = result.successCount
            val fail = result.failCount

            val toastMsg = "일정 ${success}건 등록 완료 (실패 ${fail}건))"

            showToast(toastMsg)
            findNavController().popBackStack()
        }

        photoScheduleViewModel.ocrError.observe(viewLifecycleOwner) { errorMsg ->
            showToast("등록에 실패했습니다. 다시 시도해 주세요.")
            findNavController().popBackStack()
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("ocr_image_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }
}