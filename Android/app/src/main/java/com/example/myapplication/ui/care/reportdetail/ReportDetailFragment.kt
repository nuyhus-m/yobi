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
                tvSummary.text = parseSection(report.reportContent, "주간 요약")
                tvVariation.text = parseSection(report.reportContent, "특이 변동")
                tvOverall.text = parseSection(report.reportContent, "총평")
                tvRecommendation.text = parseSection(report.reportContent, "추천 식단")
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

    /* 문자열에서 섹션만 잘라내는 헬퍼 */
    private fun parseSection(content: String?, sectionTitle: String): String {
        if (content.isNullOrBlank()) return ""
        val lines = content.split("\n")
        val start = lines.indexOfFirst {
            it.trim().removePrefix("•").trim() == sectionTitle
        }.takeIf { it != -1 } ?: return ""

        val sb = StringBuilder()
        for (i in start + 1 until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("•")) break
            sb.appendLine(lines[i])
        }
        return sb.toString().trim()
    }
}
