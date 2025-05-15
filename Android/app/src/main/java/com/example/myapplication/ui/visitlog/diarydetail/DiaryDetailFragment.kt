package com.example.myapplication.ui.visitlog.diarydetail

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentDiaryDetailBinding
import com.example.myapplication.ui.visitlog.diarydetail.viewmodel.DiaryDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
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
    private val LIST_DEST = R.id.dest_visit_log_list   // ← 자신의 destination id
    val REFRESH_KEY = "refresh_logs"

    companion object {
        private const val DELETE_RESULT_KEY = "delete_confirmed"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // 삭제 다이얼로그의 결과 관찰
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Int>(DELETE_RESULT_KEY)
            ?.observe(viewLifecycleOwner) { scheduleId ->
                savedStateHandle.remove<Int>(DELETE_RESULT_KEY)
                viewModel.deleteDailyLog(scheduleId)
            }

        // ViewModel의 데이터 관찰
        viewModel.dailyLog.observe(viewLifecycleOwner) { daily ->
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
            showToast(message)
        }

        // 삭제 성공 관찰 - 여기를 수정합니다
        viewModel.deleted.observe(viewLifecycleOwner) { success ->
            if (success) {
                showToast("삭제되었습니다.")

                // 중요: 리스트 프래그먼트에 새로고침 플래그 설정
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    REFRESH_KEY,
                    true
                )

                findNavController().popBackStack()
            }
        }

        // 데이터 로드
        viewModel.loadDailyLog(args.scheduleId)
    }
}