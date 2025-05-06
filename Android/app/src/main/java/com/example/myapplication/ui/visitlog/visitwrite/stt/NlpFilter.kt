package com.example.myapplication.ui.visitlog.visitwrite.stt

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.LanguageServiceClient
import com.google.cloud.language.v1.LanguageServiceSettings
import com.google.cloud.language.v1.ModerateTextRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.threeten.bp.Duration.ofSeconds
import java.util.concurrent.ConcurrentHashMap

class NlpFilter(ctx: Context) {
    // 최적화 1. 클라이언트 설정 개선
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val client: LanguageServiceClient = LanguageServiceClient.create(
        LanguageServiceSettings.newBuilder()
            .setCredentialsProvider { CredentialsHelper.fromAssets(ctx) }
            .setTransportChannelProvider(
                LanguageServiceSettings.defaultGrpcTransportProviderBuilder()
                    .setKeepAliveTime(ofSeconds(30))
                    .setKeepAliveTimeout(ofSeconds(10))
                    .build()
            )
            .build()
    )

    // 최적화 2. 캐싱
    private val safeTextCache = ConcurrentHashMap<String, Boolean>(100)
    private val cacheMutex = Mutex()
    private val pronouns = listOf("나는", "내가", "제가", "우리는", "우리")

    // 최적화 3. 경량 검사 단계
    private fun quickCheck(text: String): Boolean? {
        // 캐시 있으면 재사용
        safeTextCache[text]?.let {
            return it
        }
        // 짧으면 패스
        if (text.length < 3) {
            return true
        }
        // 비속어 필터링 되면 검사
        if (text.contains("*")) {
            return false
        }
        // 정밀 검사
        return null
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun isSafe(text: String): Boolean {
        // 최적화 4. 경량 검사 먼저
        quickCheck(text)?.let {
            return it
        }

        try {
            // 최적화 5. 모든 텍스트 평가 안하고 최대 길이 제한
            val truncatedText = if (text.length > 1000) text.substring(0, 1000) else text

            // 자연어 처리
            val req = ModerateTextRequest.newBuilder()
                .setDocument(
                    Document.newBuilder()
                        .setContent(truncatedText)
                        .setType(Document.Type.PLAIN_TEXT)
                        .setLanguage("ko")
                )
                .build()

            val unsafe = client.moderateText(req)
                .moderationCategoriesList.any {
                    it.confidence > 0.5f
                }

            if (unsafe) {
                cacheResult(text, false)
                return false
            }

            // 최적화 6. 1인칭 + 감정 검사 최적화
            val containOpinion = pronouns.any { truncatedText.contains(it) }

            // 최적화 7. 의견이 아니면 감정분석 패스
            if (!containOpinion) {
                cacheResult(text, true)
                return true
            }

            val sentiment = client.analyzeSentiment(
                Document.newBuilder()
                    .setContent(truncatedText)
                    .setType(Document.Type.PLAIN_TEXT)
                    .build()
            ).documentSentiment

            val strongEmotion = sentiment.magnitude > 0.8f
            val result = !(containOpinion && strongEmotion)

            cacheResult(text, result)
            return result
        } catch (e: Exception) {
            // 최적화 8. 오류 처리 개선 -> 오류시 그냥 허용
            return true
        }
    }

    // 최적화 9: 캐시 관리
    private suspend fun cacheResult(text: String, result: Boolean) {
        cacheMutex.withLock {
            // 캐시 크기 제한
            if (safeTextCache.size > 1000) {
                val keysToRemove = safeTextCache.keys.take(100)
                keysToRemove.forEach {
                    safeTextCache.remove(it)
                }
                safeTextCache[text] = result
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun shutdown() {
            client.close()
        }
    }

}