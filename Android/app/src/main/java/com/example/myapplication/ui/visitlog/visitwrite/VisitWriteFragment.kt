package com.example.myapplication.ui.visitlog.visitwrite

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentVisitWriteBinding
import com.example.myapplication.ui.visitlog.diarydetail.DiaryDetailFragment
import com.example.myapplication.ui.visitlog.visitwrite.stt.CredentialsHelper
import com.example.myapplication.ui.visitlog.visitwrite.stt.MaxLengthToastFilter
import com.example.myapplication.ui.visitlog.visitwrite.stt.NlpFilter
import com.example.myapplication.ui.visitlog.visitwrite.stt.SpeechStreamManager
import com.example.myapplication.ui.visitlog.visitwrite.viewmodel.VisitWriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "VisitWriteFragment"
private const val SKELETON_DELAY = 500L

@AndroidEntryPoint
class VisitWriteFragment : BaseFragment<FragmentVisitWriteBinding>(
    FragmentVisitWriteBinding::bind,
    R.layout.fragment_visit_write
) {

    private val args: VisitWriteFragmentArgs by navArgs()

    private val viewModel: VisitWriteViewModel by viewModels()

    // NlP
    private lateinit var nlpFilter: NlpFilter
    private val finalBuffer = StringBuilder()
    private var lastFinalChunk = ""

    // 결과 존재
    private var hasFinalResult = false

    // STT
    private lateinit var speechManager: SpeechStreamManager
    private var isRecording = false

    // 퍼미션
    private val recordAudioPermission = Manifest.permission.RECORD_AUDIO
    private val recordPermissionRequestCode = 1001

    // 길이 제한
    private val MAX_LEN = 150


    private enum class UiState { INITIAL, RECORDING, RECORDED }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        val lengthFilter = MaxLengthToastFilter(MAX_LEN) {
            showToast("더 이상 입력할 수 없습니다")
        }

        etContent.filters = arrayOf(lengthFilter)
        tvOverlayResult.filters = arrayOf(lengthFilter)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        ivMic.visibility = View.INVISIBLE
        btnRecord.visibility = View.INVISIBLE
        btnReRecordContainer.visibility = View.GONE

        if (args.isEditMode) {
            showSkeletonUI(true)

            lifecycleScope.launch {
                delay(SKELETON_DELAY)

                viewModel.loadDailyLog(
                    scheduleId = args.scheduleId,
                    onSuccess = { clientName, visitedDate, logContent ->
                        showSkeletonUI(false)

                        val title = SpannableString("${clientName}님 일지").apply {
                            setSpan(
                                RelativeSizeSpan(1.2f),
                                0,
                                clientName.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                clientName.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        binding.tvTitle.text = title

                        val dateStr = SimpleDateFormat(
                            "yyyy.MM.dd",
                            Locale.getDefault()
                        ).format(Date(visitedDate))
                        binding.tvDate.text = dateStr
                        val content = logContent ?: ""

                        binding.etContent.setText(content)
                        finalBuffer.clear()
                        finalBuffer.append(content)
                        hasFinalResult = true
                        setUiState(UiState.RECORDED)
                    }
                )
            }
        } else {
            showSkeletonUI(true)

            lifecycleScope.launch {
                delay(SKELETON_DELAY)

                viewModel.loadDailyLog(
                    scheduleId = args.scheduleId,
                    onSuccess = { clientName, visitedDate, logContent ->
                        showSkeletonUI(false)
                        val title = SpannableString("${clientName}님 일지").apply {
                            setSpan(
                                RelativeSizeSpan(1.2f),
                                0,
                                clientName.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                clientName.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        binding.tvTitle.text = title

                        val dateStr = SimpleDateFormat(
                            "yyyy.MM.dd",
                            Locale.getDefault()
                        ).format(Date(visitedDate))
                        binding.tvDate.text = dateStr
                        val content = logContent ?: ""

                        binding.etContent.setText(content)
                        finalBuffer.clear()
                        finalBuffer.append(content)
                        hasFinalResult = true
                        setUiState(UiState.INITIAL)
                    }
                )
            }

        }

        speechManager = SpeechStreamManager(requireContext())
        checkCredentialsExist()
        checkAudioPermission()

        nlpFilter = NlpFilter(requireContext())

        btnRecord.setOnClickListener {
            if (hasRecordPermission()) startRecording()
            else requestPermissions(arrayOf(recordAudioPermission), recordPermissionRequestCode)
        }

        btnStop.setOnClickListener {
            stopRecording()
        }

        btnReRecordContainer.setOnClickListener {
            finalBuffer.clear()
            lastFinalChunk = ""
            hasFinalResult = false
            startRecording()
        }

        btnComplete.setOnClickListener {
            saveVisitLog()
        }
    }

    private fun showSkeletonUI(show: Boolean) = with(binding) {
        if (show) {
            sflTitle.visibility = View.VISIBLE
            sflDate.visibility = View.VISIBLE
            sflContent.visibility = View.VISIBLE

        } else {
            sflTitle.visibility = View.GONE
            sflDate.visibility = View.GONE
            sflContent.visibility = View.GONE

            tvTitle.visibility = View.VISIBLE
            tvDate.visibility = View.VISIBLE
            etContent.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) speechManager.stopStreaming()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordPermissionRequestCode) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
            binding.btnRecord.isEnabled = granted

            if (granted) {
                binding.ivMic.clearColorFilter()
            } else {
                binding.ivMic.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.button_gray),
                    PorterDuff.Mode.SRC_IN
                )
            }
            showToast(
                if (granted) "음성 녹음 권한이 허용되었습니다"
                else "음성 녹음 권한이 거부되어 STT 기능을 사용할 수 없습니다"
            )

        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startRecording() = with(binding) {
        if (isRecording) return@with
        setUiState(UiState.RECORDING)
        isRecording = true

        setStopButtonEnabled(false) // 녹음 시작 시 중지 버튼 비활성화

        // 현재 진행 중인 중간 결과를 저장하는 변수
        var currentInterimResult = ""

        // 녹음 시작 시 초기 UI 설정
        tvOverlayResult.text = finalBuffer.toString()

        lifecycleScope.launch {
            speechManager.startStreaming()
                .catch { e ->
                    Log.e(TAG, "STT error", e)
                    showToast("음성 인식 오류: ${e.message}")
                    stopRecording()
                }
                .collect { result ->
                    if (result.isFinal) {
                        // NLP 필터를 IO 스레드에서 돌리고, UI 업데이트는 메인으로
                        launch(Dispatchers.IO) {
                            val safe = nlpFilter.isSafe(result.text)
                            // 안전한 결과인 경우에만 추가
                            if (safe) {
                                // 최대 길이 제한 체크 추가
                                val newText =
                                    if (finalBuffer.isNotEmpty()) " ${result.text}" else result.text

                                // 현재 버퍼 + 새 텍스트의 총 길이가 MAX_LEN 이하인 경우만 추가
                                if (finalBuffer.length + newText.length <= MAX_LEN) {
                                    if (finalBuffer.isNotEmpty()) finalBuffer.append(" ")
                                    finalBuffer.append(result.text)
                                    lastFinalChunk = result.text
                                    currentInterimResult = ""

                                    hasFinalResult = true

                                    withContext(Dispatchers.Main) {
                                        tvOverlayResult.text = finalBuffer.toString()
                                        setStopButtonEnabled(true)
                                    }
                                } else {
                                    // 최대 길이 초과 시 자동 저장 처리
                                    withContext(Dispatchers.Main) {
                                        showToast("최대 길이(${MAX_LEN}자)에 도달했습니다")
                                        stopRecording() // 녹음 중지
                                    }
                                }
                            }
                        }
                    } else {
                        // 사용자가 다시 말하기 시작했으면 버튼 상태 업데이트
                        currentInterimResult = result.text // 현재 중간 결과 업데이트

                        withContext(Dispatchers.Main) {
                            if (result.text.isNotEmpty()) {
                                // 사용자가 말하는 중이므로 저장 버튼 비활성화
                                setStopButtonEnabled(false)
                            }

                            // finalBuffer가 이미 최대 길이에 도달했으면 중간 결과 표시하지 않음
                            if (finalBuffer.length >= MAX_LEN) {
                                tvOverlayResult.text = finalBuffer.toString()
                                return@withContext
                            }

                            // 추가될 중간 결과가 최대 길이를 초과하는지 확인
                            val combinedLength =
                                finalBuffer.length + (if (finalBuffer.isNotEmpty()) 1 else 0) + currentInterimResult.length

                            // 최종 결과(finalBuffer)와 현재 중간 결과(currentInterimResult)를 결합하여 표시
                            val preview = if (finalBuffer.isEmpty()) {
                                if (combinedLength <= MAX_LEN) {
                                    currentInterimResult
                                } else {
                                    currentInterimResult.substring(0, MAX_LEN - finalBuffer.length)
                                }
                            } else if (currentInterimResult.isEmpty()) {
                                finalBuffer.toString()
                            } else {
                                if (combinedLength <= MAX_LEN) {
                                    "${finalBuffer.toString()} ${currentInterimResult}"
                                } else {
                                    // 최대 길이를 초과하는 경우 표시 가능한 만큼만 표시
                                    val availableSpace =
                                        MAX_LEN - finalBuffer.length - 1 // 공백 1자리 고려
                                    val displayInterim = if (availableSpace > 0) {
                                        currentInterimResult.substring(
                                            0,
                                            minOf(availableSpace, currentInterimResult.length)
                                        )
                                    } else {
                                        ""
                                    }
                                    if (displayInterim.isNotEmpty()) {
                                        "${finalBuffer.toString()} $displayInterim"
                                    } else {
                                        finalBuffer.toString()
                                    }
                                }
                            }
                            tvOverlayResult.text = preview
                        }
                    }
                }
        }
    }

    private fun stopRecording() = with(binding) {
        if (!isRecording) return

        if (!hasFinalResult) {
            showToast("음성 인식 결과가 아직 없습니다. 잠시 후 다시 시도해 주세요.")
            return
        }

        speechManager.stopStreaming()
        isRecording = false

        etContent.setText(finalBuffer.toString().trim())
        setUiState(UiState.RECORDED)
    }

    private fun setStopButtonEnabled(enabled: Boolean) = with(binding.btnStop) {
        isEnabled = enabled
        text = if (enabled) {
            getString(R.string.save)
        } else {
            getString(R.string.recording)
        }

        val bgColor = if (enabled) {
            R.color.purple
        } else {
            R.color.gray_recording
        }
        background.setColorFilter(
            ContextCompat.getColor(requireContext(), bgColor),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun setUiState(state: UiState) = with(binding) {
        when (state) {
            UiState.INITIAL -> {
                // 메인
                ivMic.visibility = View.VISIBLE
                btnRecord.visibility = View.VISIBLE
                btnReRecordContainer.visibility = View.GONE
                etContent.visibility = View.GONE
                cvContent.visibility = View.GONE
                setCompleteButtonEnabled(false)

                // 오버레이
                flOverlay.visibility = View.GONE
                ivMicBig.visibility = View.GONE
            }

            UiState.RECORDING -> {
                // 메인
                ivMic.visibility = View.INVISIBLE
                btnRecord.visibility = View.INVISIBLE
                btnReRecordContainer.visibility = View.GONE
                etContent.visibility = View.GONE
                cvContent.visibility = View.GONE
                setCompleteButtonEnabled(false)

                // 오버레이
                tvOverlayResult.text = ""
                flOverlay.visibility = View.VISIBLE
                ivMicBig.visibility = View.VISIBLE

                setStopButtonEnabled(false)

            }

            UiState.RECORDED -> {
                // 메인
                ivMic.visibility = View.GONE
                btnRecord.visibility = View.GONE
                btnReRecordContainer.visibility = View.VISIBLE
                etContent.visibility = View.VISIBLE
                cvContent.visibility = View.VISIBLE
                setCompleteButtonEnabled(true)

                // 오버레이
                flOverlay.visibility = View.GONE
                ivMicBig.visibility = View.GONE

            }
        }
    }

    private fun setCompleteButtonEnabled(enabled: Boolean) = with(binding.btnComplete) {
        isEnabled = enabled
    }

    private fun saveVisitLog() {
        val content = binding.etContent.text.toString().trim()

        if (content.isEmpty()) {
            showToast("일지 내용을 입력해주세요")
            return
        }

        val schedueId = args.scheduleId
        viewModel.saveDailyLog(
            scheduleId = schedueId,
            content = content,
            onSuccess = {
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(DiaryDetailFragment.EDIT_DONE_RESULT_KEY, true)

                showToast("일지가 저장되었습니다.")
                Navigation.findNavController(requireView()).popBackStack()
            }
        )
    }

    private fun checkAudioPermission() {
        if (!hasRecordPermission()) {
            requestPermissions(arrayOf(recordAudioPermission), recordPermissionRequestCode)

            binding.ivMic.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.button_gray),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.ivMic.clearColorFilter()

        }

    }

    private fun hasRecordPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            recordAudioPermission
        ) == PackageManager.PERMISSION_GRANTED

    private fun checkCredentialsExist() {
        if (!CredentialsHelper.checkCredentialsExist(requireContext())) {
            showToast("Google Cloud 인증 파일이 없습니다. STT 기능을 사용할 수 없습니다.")
            binding.btnRecord.isEnabled = false
        }
    }
}