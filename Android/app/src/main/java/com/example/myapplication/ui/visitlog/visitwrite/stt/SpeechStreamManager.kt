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
import kotlin.math.abs

private const val TAG = "SpeechStreamManager"

data class Transcript(val text: String, val isFinal: Boolean)

class SpeechStreamManager(private val ctx: Context) {
    private val sampleRate = 16_000
    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    // 최적화 1. 버퍼 크기 키우기 (너무 작으면 인식 자체를 못할 문제 발생)
    private val bufferSize = minBufferSize * 2

    // 최적화 2. 음성 감지 임계값 설정하기
    private val silenceThreshold = 500
    private var lastActiveAudioTime = 0L
    private val voiceActivityTimeout = 1000L

    // 최적화 3. 전용 스레드 풀 사용 (안쓰면... 병목현상)
    private val audioExecutor = Executors.newSingleThreadExecutor()
    private val processingExecutor = Executors.newSingleThreadExecutor()

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

        // 이미 스트리밍 중인 경우 중지
        if (isStreaming.getAndSet(true)) {
            stopStreamingInternal()
        }

        try {
            // 최적화 4. gRPC 채널 및 자격 + 채널 옵션 추가
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

            // 최적화 5. 요청 config 추가
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
                // 최적화 6: 음성 종료 감지 설정
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

            // 최적화 8. 전용 스레드에서 오디오 처리하기 (비동기)
            launch(Dispatchers.IO) {
                val buf = ByteArray(bufferSize)
                val shortBuf = ShortArray(bufferSize / 2)

                // 쉿..한 프레임 수
                var consecutiveSilenceFrames = 0
                // 마지막 음성 시간 기록 초기화
                lastActiveAudioTime = System.currentTimeMillis()

                while (isActive && isStreaming.get()) {
                    val len = audioRecord?.read(buf, 0, buf.size) ?: -1
                    if (len > 0) {
                        // 최적화 9: 음성 활동 감지(VAD)
                        // 16비트 샘플을 short 배열로 변환
                        // 이부분을 통해 음성 세기 계산.
                        for (i in 0 until len / 2) {
                            shortBuf[i] =
                                (buf[i * 2 + 1].toInt() shl 8 or (buf[i * 2].toInt() and 0xFF)).toShort()
                        }

                        // 간단한 에너지 기반 VAD ( 높은 에너지는 말하는것, 낮은건 침묵)
                        var energy = 0.0
                        for (i in 0 until len / 2) {
                            energy += abs(shortBuf[i].toDouble())
                        }
                        energy /= (len / 2)

                        if (energy > silenceThreshold) {
                            lastActiveAudioTime = System.currentTimeMillis()
                            consecutiveSilenceFrames = 0
                            // 활성 음성이 감지되면 즉시 전송
                            requestObserver?.onNext(
                                StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(ByteString.copyFrom(buf, 0, len))
                                    .build()
                            )
                        } else {
                            consecutiveSilenceFrames++
                            // 침묵이 짧으면 전송 (문장 중간의 짧은 멈춤)
                            if (consecutiveSilenceFrames < 5) {
                                requestObserver?.onNext(
                                    StreamingRecognizeRequest.newBuilder()
                                        .setAudioContent(ByteString.copyFrom(buf, 0, len))
                                        .build()
                                )
                            }
                            // 침묵이 길면 낮은 빈도로 전송 (네트워크 트래픽 절약)
                            else if (consecutiveSilenceFrames % 3 == 0) {
                                requestObserver?.onNext(
                                    StreamingRecognizeRequest.newBuilder()
                                        .setAudioContent(ByteString.copyFrom(buf, 0, len))
                                        .build()
                                )
                            }
                        }
                    }

                    // 최적화 10: 장시간 침묵 시 처리
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
            requestObserver = null
        } catch (e: Exception) {
            Log.e(TAG, "Error completing request observer", e)
        }
    }


    /**
     * 리소스 완전히 정리 (앱 종료 시에만 호출)
     */
    fun shutdown() {
        stopStreaming()
        audioExecutor.shutdown()
        processingExecutor.shutdown()
        try {
            // 최대 2초 기다림
            if (!audioExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                audioExecutor.shutdownNow()
            }
            if (!processingExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            audioExecutor.shutdownNow()
            processingExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    // 유틸리티 함수
    private fun min(a: Int, b: Int): Int = if (a <= b) a else b
}