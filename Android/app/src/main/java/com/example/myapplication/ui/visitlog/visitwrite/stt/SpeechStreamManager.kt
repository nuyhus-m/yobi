package com.example.myapplication.ui.visitlog.visitwrite.stt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechGrpc
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import com.google.protobuf.ByteString
import com.konovalov.vad.webrtc.VadWebRTC
import com.konovalov.vad.webrtc.config.FrameSize
import com.konovalov.vad.webrtc.config.Mode
import com.konovalov.vad.webrtc.config.SampleRate
import io.grpc.ManagedChannelBuilder
import io.grpc.auth.MoreCallCredentials
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SpeechStreamManager"

data class Transcript(val text: String, val isFinal: Boolean)

class SpeechStreamManager(private val ctx: Context) {
    private val sampleRate = 16_000
    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    // VAD (에너지 계산) 핸들
    private var vad: VadWebRTC? = null

    // 최적화 1. 버퍼 크기 키우기 (너무 작으면 인식 자체를 못할 문제 발생)
    private val bufferSize = minBufferSize * 2

    // 최적화 2. 음성 감지 임계값 설정하기
    private var lastActiveAudioTime = 0L
    private val voiceActivityTimeout = 1000L

    // 스트리밍 상태 추적
    private val isStreaming = AtomicBoolean(false)
    private var audioRecord: AudioRecord? = null
    private var requestObserver: StreamObserver<StreamingRecognizeRequest>? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startStreaming(): Flow<Transcript> = callbackFlow {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            close(SecurityException("RECORD_AUDIO permission not granted"))
            return@callbackFlow
        }

        vad = VadWebRTC(
            sampleRate = SampleRate.SAMPLE_RATE_16K,
            frameSize = FrameSize.FRAME_SIZE_320,   // 320 samples → 20ms @ 16kHz
            mode = Mode.VERY_AGGRESSIVE,        // 감도(침묵 억제) 높이기
            silenceDurationMs = 300, // 침묵 최소 0.3초
            speechDurationMs = 50 // 0.05초 이상 꾸준히 말해야 sppech

        )

        // 이미 스트리밍 중인 경우 중지
        if (isStreaming.getAndSet(true)) {
            stopStreamingInternal()
        }

        try {
            // 최적화 3. gRPC 채널 및 자격 + 채널 옵션 추가
            val creds = CredentialsHelper.fromAssets(ctx)
            val channel = ManagedChannelBuilder
                .forAddress("speech.googleapis.com", 443)
                .useTransportSecurity()
                .keepAliveTime(60, TimeUnit.SECONDS) // 연결을 유지하고
                .keepAliveTimeout(20, TimeUnit.SECONDS)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10메가
                .build()

            val stub = SpeechGrpc.newStub(channel)
                .withCallCredentials(MoreCallCredentials.from(creds))
                .withDeadlineAfter(60, TimeUnit.SECONDS) // 타임아웃 설정

            // 최적화 4. 요청 config 추가
            val config = RecognitionConfig.newBuilder()
                .setLanguageCode("ko-KR") // 한국어 특화 (외국어가 필요할 경우,setAlternativeLanguageCodes()를 쓰기)
                .setSampleRateHertz(sampleRate)
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setEnableAutomaticPunctuation(true) // 쉼표등 구분
                .setProfanityFilter(true) // 욕설 필터링
                .setAudioChannelCount(1) // 단일 채널
                .setUseEnhanced(true) // 적응형 모델 사용
                .setModel("latest_long") // 긴 음성에 최적화된 모델 사용
                .build()

            val streamConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(config)
                .setInterimResults(true) // 중간 결과 반환
                // 최적화 5: 음성 종료 감지 설정
                .setSingleUtterance(false) // 여러 발화 허용
                .build()

            // 응답 처리 Observer
            requestObserver =
                stub.streamingRecognize(object : StreamObserver<StreamingRecognizeResponse> {
                    override fun onNext(value: StreamingRecognizeResponse) {
                        value.resultsList.forEach { r ->
                            if (r.alternativesList.isNotEmpty()) {
                                val transcript =
                                    Transcript(r.alternativesList.first().transcript, r.isFinal)
                                trySend(transcript)
                                Log.d(
                                    TAG,
                                    "Transcript: ${transcript.text}, isFinal: ${transcript.isFinal}"
                                )
                            }
                        }
                    }

                    override fun onError(t: Throwable) {
                        Log.e(TAG, "Speech recognition error", t)
                        trySend(Transcript("오류 발생: ${t.message}", true))
                        isStreaming.set(false)
                        channel?.shutdown()
                    }

                    override fun onCompleted() {
                        Log.d(TAG, "Speech recognition completed")
                        isStreaming.set(false)
                        channel?.shutdown()
                    }
                })

            // 첫 패킷: 설정 전송
            requestObserver?.onNext(
                StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamConfig)
                    .build()
            )

            // 마이크 캡처 (권한 확보 후이므로 SecurityException 안전)
            // 오디오 프로세싱 모드 우선순위 높이기
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            ).apply { startRecording() }

            // 최적화 6. 전용 스레드에서 오디오 처리하기 (비동기)
            launch(Dispatchers.IO) {

                // 중요!! vad-webRTC는 640프레임만 받음!!
                val frameBytes = FrameSize.FRAME_SIZE_320.value * 2
                val buf = ByteArray(bufferSize)

                // 쉿..한 프레임 수
                var consecutiveSilenceFrames = 0
                // 마지막 음성 시간 기록 초기화
                lastActiveAudioTime = System.currentTimeMillis()

                while (isActive && isStreaming.get()) {
                    val len = audioRecord?.read(buf, 0, buf.size) ?: -1
                    if (len == frameBytes) {                               // 정확히 1프레임이면
                        val speech = vad?.isSpeech(buf) == true            // ★ WebRTC VAD 호출

                        if (speech) {
                            lastActiveAudioTime = System.currentTimeMillis()
                            consecutiveSilenceFrames = 0
                            requestObserver?.onNext(
                                StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(ByteString.copyFrom(buf))
                                    .build()
                            )
                        } else {
                            consecutiveSilenceFrames++
                            // 짧은 침묵 → 그대로 전송
                            if (consecutiveSilenceFrames < 5 ||
                                consecutiveSilenceFrames % 3 == 0
                            ) {
                                requestObserver?.onNext(
                                    StreamingRecognizeRequest.newBuilder()
                                        .setAudioContent(ByteString.copyFrom(buf))
                                        .build()
                                )
                            }
                        }
                    }

                    // 최적화 7: 장시간 침묵 시 처리
                    if (System.currentTimeMillis() - lastActiveAudioTime > voiceActivityTimeout * 5 &&
                        lastActiveAudioTime > 0
                    ) {
                        Log.d(
                            TAG,
                            "Long silence detected, sending more frequent keep-alive packets"
                        )
                        // 긴 침묵 동안 더 자주 작은 패킷을 보내서 연결 유지
                        if (consecutiveSilenceFrames % 2 == 0) {
                            requestObserver?.onNext(
                                StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(ByteString.copyFrom(buf, 0, min(len, 512)))
                                    .build()
                            )
                        }
                    }
                }

                // 루프 종료 후 정리
                stopAudioRecording()
                completeRequestObserver()
            }

            awaitClose {
                stopStreamingInternal()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting streaming", e)
            isStreaming.set(false)
            close(e)
        }
    }

    /**
     * 스트리밍 중지 및 리소스 정리
     */
    fun stopStreaming() {
        if (isStreaming.getAndSet(false)) {
            stopStreamingInternal()
        }
    }

    private fun stopStreamingInternal() {
        Log.d(TAG, "Stopping streaming")
        stopAudioRecording()
        completeRequestObserver()
    }

    private fun stopAudioRecording() {
        try {
            audioRecord?.let {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
                it.release()
                audioRecord = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }
    }

    private fun completeRequestObserver() {
        try {
            requestObserver?.onCompleted()
        } catch (_: Exception) {

        }
        requestObserver = null
        vad?.close()            // ★ VAD 메모리 해제
        vad = null
    }


    /**
     * 리소스 완전히 정리 (앱 종료 시에만 호출)
     */

    // 유틸리티 함수
    private fun min(a: Int, b: Int): Int = if (a <= b) a else b
}
