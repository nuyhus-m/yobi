package com.example.myapplication.ui.visitlog.diarydetail

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentDiaryDetailBinding
import com.example.myapplication.ui.visitlog.diarydetail.viewmodel.DiaryDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DiaryDetailFragment : BaseFragment<FragmentDiaryDetailBinding>(
    FragmentDiaryDetailBinding::bind,
    R.layout.fragment_diary_detail
) {
    private val args: DiaryDetailFragmentArgs by navArgs()
    private val viewModel: DiaryDetailViewModel by viewModels()

    companion object {
        private const val DELETE_RESULT_KEY = "delete_confirmed"
        const val EDIT_DONE_RESULT_KEY = "edit_done"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()


        observeEditResult()

        // 데이터 로드 전에 shimmer 시작
        showShimmerEffect(true)

        // 데이터 로드
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            viewModel.loadDailyLog(args.scheduleId)
        }
    }

    private fun observeEditResult() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>(EDIT_DONE_RESULT_KEY)
            ?.observe(viewLifecycleOwner) { edited ->
                if (edited == true) {
                    // 일회성 결과이므로 바로 제거
                    savedStateHandle.remove<Boolean>(EDIT_DONE_RESULT_KEY)

                    // shimmer 다시 켜고 최신 데이터 요청
                    viewLifecycleOwner.lifecycleScope.launch {
                        showShimmerEffect(true)        // skeleton ON
                        delay(500)          // 0.5 s 지연
                        viewModel.loadDailyLog(args.scheduleId)
                    }
                }
            }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDelete.setOnClickListener {
            val action = DiaryDetailFragmentDirections
                .actionDiaryDetailFragmentToDestDeleteDairyDialog(args.scheduleId)
            findNavController().navigate(action)
        }

        binding.btnEdit.setOnClickListener {
            val action = DiaryDetailFragmentDirections
                .actionDestDiaryDetailFragmentToDestVisitWriteFragment(
                    scheduleId = args.scheduleId,
                    isEditMode = true
                )
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        // 삭제 다이얼로그의 결과 관찰
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Int>(DELETE_RESULT_KEY)
            ?.observe(viewLifecycleOwner) { scheduleId ->
                savedStateHandle.remove<Int>(DELETE_RESULT_KEY)
                viewModel.deleteDailyLog(scheduleId)
            }

        // ViewModel의 데이터 관찰
        viewModel.dailyLog.observe(viewLifecycleOwner) { daily ->

            showShimmerEffect(false)

            binding.apply {
                val name = daily.clientName
                val fullText = "${name}님 일지"
                val spannable = SpannableString(fullText)
                val nameEnd = name.length
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    nameEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    AbsoluteSizeSpan(24, true),
                    0,
                    nameEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvTitle.text = spannable
                tvContent.text = daily.logContent
                val formattedDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                    .format(Date(daily.visitedDate))
                tvDate.text = formattedDate
            }
        }

        // 에러 메시지 관찰
        viewModel.error.observe(viewLifecycleOwner) { message ->
            // 에러 발생해도 shimmer 효과 중지
            showShimmerEffect(false)
            showToast(message)
        }

        // 삭제 성공 관찰
        viewModel.deleted.observe(viewLifecycleOwner) { success ->
            if (success) {
                showToast("삭제되었습니다.")
                findNavController().popBackStack()
            }
        }
    }

    private fun showShimmerEffect(show: Boolean) {
        if (show) {
            // 텍스트 뷰 숨기고 shimmer 표시
            binding.tvTitle.visibility = View.INVISIBLE
            binding.tvDate.visibility = View.GONE
            binding.tvContent.visibility = View.INVISIBLE

            binding.shimmerTitle.visibility = View.VISIBLE
            binding.shimmerDate.visibility = View.VISIBLE
            binding.shimmerContent.visibility = View.VISIBLE

            binding.shimmerTitle.startShimmer()
            binding.shimmerDate.startShimmer()
            binding.shimmerContent.startShimmer()
        } else {
            // 텍스트 뷰 표시하고 shimmer 숨김
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvDate.visibility = View.VISIBLE
            binding.tvContent.visibility = View.VISIBLE

            binding.shimmerTitle.visibility = View.GONE
            binding.shimmerDate.visibility = View.GONE
            binding.shimmerContent.visibility = View.GONE

            binding.shimmerTitle.stopShimmer()
            binding.shimmerDate.stopShimmer()
            binding.shimmerContent.stopShimmer()
        }
    }
}