package com.example.myapplication.ui.measure.measureloading

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentMeasureLoadingBinding
import com.example.myapplication.ui.FitrusViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureLoadingFragment : BaseFragment<FragmentMeasureLoadingBinding>(
    FragmentMeasureLoadingBinding::bind,
    R.layout.fragment_measure_loading
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
        setTitle()
        initView()
    }

    private fun initButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setTitle() {
        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.clientName)
    }

    private fun initView() {
        binding.ivCharacter.post {
            val animationDrawable = binding.ivCharacter.background as AnimationDrawable
            animationDrawable.start()
        }
    }
}