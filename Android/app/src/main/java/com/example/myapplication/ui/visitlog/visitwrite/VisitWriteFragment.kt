package com.example.myapplication.ui.visitlog.visitwrite

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
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
    private lateinit var nlpFilter: NlpFilter          // ← 추가
    private val finalBuffer = StringBuilder()          // ← 누적용 버퍼
    private var lastFinalChunk = ""
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
                RelativeSizeSpan(1.3f), // 1.3배 크기
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


        nlpFilter = NlpFilter(requireContext())        // ← 추가


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

        lifecycleScope.launch {
            speechManager.startStreaming()
                .catch { e ->
                    Log.e(TAG, "STT error", e)
                    showToast("음성 인식 오류: ${e.message}")
                    stopRecording()
                }
                .collect { result ->
                    if (result.isFinal) {          // ── FINAL ──
                        // NLP 필터를 IO 스레드에서 돌리고, UI 업데이트는 메인으로
                        launch(Dispatchers.IO) {
                            val safe = nlpFilter.isSafe(result.text)
                            if (safe && result.text != lastFinalChunk) {
                                if (finalBuffer.isNotEmpty()) finalBuffer.append(" ")
                                finalBuffer.append(result.text)
                                lastFinalChunk = result.text
                                withContext(Dispatchers.Main) {
                                    tvOverlayResult.text = finalBuffer.toString()
                                }
                            }
                        }
                    } else {                       // ── INTERIM ──
                        // 필터 없이 ‘실시간’으로 바로 보여주기만
                        val preview = if (finalBuffer.isEmpty())
                            result.text
                        else
                            "${finalBuffer} ${result.text}"
                        tvOverlayResult.text = preview
                    }
                }
        }
    }

    /* ─────────────────  녹음 중지 ───────────────── */
    private fun stopRecording() = with(binding) {
        if (!isRecording) return
        speechManager.stopStreaming()
        isRecording = false

        etContent.setText(finalBuffer.toString().trim())   // 누적본 반영

        finalBuffer.clear()            // 다음 녹음을 위해 리셋
        lastFinalChunk = ""
        setUiState(UiState.RECORDED)
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
        val color = if (enabled) R.color.purple else R.color.gray_text_secondary
        background.setColorFilter(
            ContextCompat.getColor(requireContext(), color),
            PorterDuff.Mode.SRC_IN
        )
    }


    // 지금은 그냥 dialog 띄우고 뒤로가는걸로 되어있습니다.
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
