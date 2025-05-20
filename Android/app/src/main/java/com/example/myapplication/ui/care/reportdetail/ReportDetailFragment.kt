package com.example.myapplication.ui.care.reportdetail

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentReportDetailBinding
import com.example.myapplication.ui.care.reportdetail.viewmodel.ReportDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ReportDetailFragment"

@AndroidEntryPoint
class ReportDetailFragment : BaseFragment<FragmentReportDetailBinding>(
    FragmentReportDetailBinding::bind,
    R.layout.fragment_report_detail
) {
    private val args: ReportDetailFragmentArgs by navArgs()
    private val viewModel: ReportDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reportId = args.reportId
        val dateRange = args.dateRange
        val name: String = findNavController().previousBackStackEntry
            ?.savedStateHandle?.get<String>("clientName") ?: ""

        val titleSpannable = SpannableStringBuilder().apply {
            val spanName = SpannableString(name).apply {
                setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(AbsoluteSizeSpan(24, true), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(spanName)
            append("님 주간 보고서")
        }
        binding.tvTitle.text = titleSpannable
        binding.tvDateRange.text = dateRange

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        with(binding) {
            listOf(
                sflTvTitle, sflDate, sflSummary, sflVariation,
                sflOverall, sflRecommendation, sflWeekLog
            ).forEach { it.apply { visibility = View.VISIBLE; startShimmer() } }

            listOf(
                tvTitle, tvDateRange, tvSummary, tvVariation,
                tvOverall, tvRecommendation, tvWeekLog
            ).forEach { it.visibility = View.INVISIBLE }
        }

        viewModel.fetchReportDetail(reportId)

        viewModel.report.observe(viewLifecycleOwner) { report ->
            binding.apply {
                // 예: 추천 식단 섹션 → 여러 개 블록으로
                val recos = parseSectionBlocks(report.reportContent, "추천 식단")
                Log.d(TAG, "onViewCreated: $recos")
                // 블록 사이에 빈 줄 하나 넣어서 화면에 보여줌
                tvRecommendation.text = recos.joinToString("\n\n")

                // 다른 섹션도 필요하다면 동일하게
                val summary = parseSectionBlocks(report.reportContent, "주간 요약")
                Log.d(TAG, "onViewCreated: $summary")

                tvSummary.text = summary.joinToString("\n\n")

                val variation = parseSectionBlocks(report.reportContent, "특이 변동")
                tvVariation.text = variation.joinToString("\n\n")

                // “총평” 같은 한 덩어리짜리 섹션도 잘 나옵니다.
                val overall = parseSectionBlocks(report.reportContent, "총평")
                tvOverall.text = overall.joinToString("\n\n")

                // 일지 요약은 이미 별도 필드
                tvWeekLog.text = report.logSummery
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500L)

            with(binding) {
                listOf(
                    sflTvTitle, sflDate, sflSummary, sflVariation,
                    sflOverall, sflRecommendation, sflWeekLog
                ).forEach {
                    it.stopShimmer()
                    it.visibility = View.GONE
                }

                listOf(
                    tvTitle, tvDateRange, tvSummary, tvVariation,
                    tvOverall, tvRecommendation, tvWeekLog
                ).forEach {
                    it.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * content 내에 literal "\n" 을 실제 개행으로 치환하고,
     * "• sectionTitle" 섹션의 본문을 뽑아서
     * 1차 불릿(-)마다 하나의 블록으로 묶은 List<String>을 반환합니다.
     */
    private fun parseSectionBlocks(content: String?, sectionTitle: String): List<String> {
        if (content.isNullOrBlank()) return emptyList()

        // 데이터 전처리: 문자열 "\n"을 실제 개행문자로 변환하고 특수한 케이스 처리
        var normalized = content.replace("""\n""", "\n")

        // "도\n•"와 같은 깨진 텍스트 수정 (추천 식단 섹션의 특수 케이스)
        if (sectionTitle == "추천 식단") {
            normalized = normalized.replace("도\n• 일지 요약", "도움이 됩니다.\n• 일지 요약")
        }

        // 로깅
        Log.d(TAG, "Normalized text for section '$sectionTitle': ${normalized.take(100)}...")

        // 섹션 내용 추출
        val sectionPattern = "•\\s*${Regex.escape(sectionTitle)}\\s*(.*?)(?=\\s*•|$)"
        val sectionMatch = Regex(sectionPattern, RegexOption.DOT_MATCHES_ALL).find(normalized)

        if (sectionMatch == null) {
            Log.d(TAG, "Section not found: $sectionTitle")
            return emptyList()
        }

        val sectionText = sectionMatch.groupValues[1].trim()
        Log.d(TAG, "Section text for '$sectionTitle': ${sectionText.take(100)}...")

        // 모든 불릿 항목 추출
        val result = mutableListOf<String>()
        val bulletLines = sectionText.split("\n").filter { it.trim().startsWith("-") }

        for (i in bulletLines.indices) {
            val currentBullet = bulletLines[i].trim()
            val nextBulletIndex = if (i < bulletLines.size - 1) {
                sectionText.indexOf(bulletLines[i + 1], sectionText.indexOf(currentBullet) + currentBullet.length)
            } else {
                sectionText.length
            }

            // 현재 불릿부터 다음 불릿 직전까지 추출
            val startIndex = sectionText.indexOf(currentBullet)
            val endIndex = if (nextBulletIndex > startIndex) nextBulletIndex else sectionText.length

            if (startIndex >= 0 && endIndex > startIndex) {
                val bulletWithDesc = sectionText.substring(startIndex, endIndex).trim()
                result.add(bulletWithDesc)
            }
        }

        Log.d(TAG, "Found ${result.size} bullet points for section '$sectionTitle'")
        return result
    }
}