package com.example.myapplication.ui.care.reportdetail

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentReportDetailBinding
import com.example.myapplication.ui.care.reportdetail.viewmodel.ReportDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
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
        val name = args.name
        val dateRange = args.dateRange

        // 타이틀 텍스트 스타일링
        val fullTitle = "${name}님 주간 보고서"
        val spannable = SpannableString(fullTitle).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(AbsoluteSizeSpan(24, true), 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvTitle.text = spannable
        binding.tvDateRange.text = dateRange

        // 데이터 요청
        viewModel.fetchReportDetail(reportId)

        // 데이터 바인딩
        viewModel.report.observe(viewLifecycleOwner) { report ->
            binding.apply {
                // log_summary: 주간 요약
                tvSummary.text = report.log_summary

                tvVariation.text = parseSection(report.report_content, "특이 변동")
                tvOverall.text = parseSection(report.report_content, "총평")
                tvRecommendation.text = parseSection(report.report_content, "추천 식단")
            }
        }
    }

    // 예시: report_content 문자열을 파싱해서 각 섹션별 내용만 추출
    private fun parseSection(content: String, sectionTitle: String): String {
        val lines = content.split("\n")
        val startIndex = lines.indexOfFirst { it.trim() == "• $sectionTitle" }
        if (startIndex == -1) return ""

        val result = StringBuilder()
        for (i in startIndex + 1 until lines.size) {
            if (lines[i].startsWith("•")) break
            result.appendLine(lines[i])
        }
        return result.toString().trim()
    }
}
