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

class DiaryDetailFragment : BaseFragment<FragmentDiaryDetailBinding>(
    FragmentDiaryDetailBinding::bind,
    R.layout.fragment_diary_detail

) {

    private val args: DiaryDetailFragmentArgs by navArgs()
    private val viewModel: DiaryDetailViewModel by viewModels()

    companion object {
        private const val DELETE_RESULT_KEY = "delete_confirmed"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDelete.setOnClickListener {
            val action = DiaryDetailFragmentDirections
                .actionDiaryDetailFragmentToDestDeleteDairyDialog(args.scheduleId)
            findNavController().navigate(action)
        }
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Int>(DELETE_RESULT_KEY)
            ?.observe(viewLifecycleOwner) { scheduleId ->
                savedStateHandle.remove<Int>(DELETE_RESULT_KEY)
                viewModel.deleteDailyLog(scheduleId)
            }


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

        viewModel.error.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
        viewModel.deleted.observe(viewLifecycleOwner) { success ->
            if (success) {
                showToast("삭제되었습니다.")
                findNavController().popBackStack()
            }
        }
        viewModel.loadDailyLog(args.scheduleId)
    }


}