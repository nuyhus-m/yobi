package com.example.myapplication.ui.visitlog.visitwrite

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentVisitWriteBinding
import com.example.myapplication.ui.visitlog.visitwrite.stt.CredentialsHelper
import com.example.myapplication.ui.visitlog.visitwrite.stt.NlpFilter
import com.example.myapplication.ui.visitlog.visitwrite.stt.SpeechStreamManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "VisitWriteFragment"

@AndroidEntryPoint
class VisitWriteFragment : BaseFragment<FragmentVisitWriteBinding>(
    FragmentVisitWriteBinding::bind,
    R.layout.fragment_visit_write
) {

    private val args: VisitWriteFragmentArgs by navArgs()

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

    private enum class UiState { INITIAL, RECORDING, RECORDED }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        val name = args.name
        val fullText = "${name}님 일지"

        val spannable = SpannableString(fullText).apply {
            setSpan(
                RelativeSizeSpan(1.2f),
                0,
                name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvTitle.text = spannable
        tvDate.text = args.date

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
            startRecording()
        }

        btnComplete.setOnClickListener {
            saveVisitLog()
        }

        setUiState(UiState.INITIAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) speechManager.stopStreaming()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startRecording() = with(binding) {
        if (isRecording) return@with
        setUiState(UiState.RECORDING)
        isRecording = true
        hasFinalResult = false
        setStopButtonEnabled(false) // 녹음 시작 시 중지 버튼 비활성화

        finalBuffer.clear()
        lastFinalChunk = ""

        // 수집된 최종 결과를 저장하기 위한 집합
        val processedFinalResults = mutableSetOf<String>()

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
                            // 안전하고 이전에 처리되지 않은 결과인 경우에만 추가
                            if (safe && !processedFinalResults.contains(result.text)) {
                                processedFinalResults.add(result.text)
                                if (finalBuffer.isNotEmpty()) finalBuffer.append(" ")
                                finalBuffer.append(result.text)
                                lastFinalChunk = result.text

                                hasFinalResult = true

                                withContext(Dispatchers.Main) {
                                    tvOverlayResult.text = finalBuffer.toString()
                                    setStopButtonEnabled(true)
                                }
                            }
                        }
                    } else {
                        val preview = if (finalBuffer.isEmpty())
                            result.text
                        else
                            "${finalBuffer} ${result.text}"
                        tvOverlayResult.text = preview
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
        showToast("일지가 저장되었습니다")
        Navigation.findNavController(requireView()).popBackStack()
    }


    private fun checkAudioPermission() {
        if (!hasRecordPermission())
            requestPermissions(arrayOf(recordAudioPermission), recordPermissionRequestCode)
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
            showToast(
                if (granted) "음성 녹음 권한이 허용되었습니다"
                else "음성 녹음 권한이 거부되어 STT 기능을 사용할 수 없습니다"
            )
        }
    }
}
