package com.example.myapplication.ui.measure.measureresult

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.base.GradeType
import com.example.myapplication.base.HealthDataType
import com.example.myapplication.data.dto.response.measure.BloodPressureResponse
import com.example.myapplication.data.dto.response.measure.BloodPressureResultResponse
import com.example.myapplication.data.dto.response.measure.BodyCompositionResponse
import com.example.myapplication.data.dto.response.measure.BodyCompositionResultResponse
import com.example.myapplication.data.dto.response.measure.HealthDataResponse
import com.example.myapplication.data.dto.response.measure.HeartRateResponse
import com.example.myapplication.data.dto.response.measure.HeartRateResultResponse
import com.example.myapplication.data.dto.response.measure.RequiredDataResponse
import com.example.myapplication.data.dto.response.measure.StressResponse
import com.example.myapplication.data.dto.response.measure.StressResultResponse
import com.example.myapplication.data.dto.response.measure.TemperatureResponse
import com.example.myapplication.data.dto.response.measure.TemperatureResultResponse
import com.example.myapplication.databinding.FragmentMeasureResultBinding
import com.example.myapplication.databinding.LayoutResultEightBinding
import com.example.myapplication.databinding.LayoutResultItemBinding
import com.example.myapplication.databinding.LayoutResultOneBinding
import com.example.myapplication.databinding.LayoutResultTwoBinding
import com.example.myapplication.ui.FitrusViewModel
import com.example.myapplication.ui.measure.measureresult.viewmodel.MeasureResultViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeasureResultFragment : BaseFragment<FragmentMeasureResultBinding>(
    FragmentMeasureResultBinding::bind,
    R.layout.fragment_measure_result
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()
    private val viewModel by viewModels<MeasureResultViewModel>()
    private val args by navArgs<MeasureResultFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBackButton()
        setCompleteButton()
        setText()
        observeHealthDataResult()
        getResultData(fitrusViewModel.healthDataResponse)
    }

    private fun initBackButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setCompleteButton() {
        if (args.isRequiredBodyComp) {
            binding.btnComplete.text = getString(R.string.next)
            binding.btnComplete.setOnClickListener {
                findNavController().navigate(R.id.dest_measure_result)
            }
        } else {
            binding.btnComplete.text = getString(R.string.complete)
            binding.btnComplete.setOnClickListener {
                findNavController().navigate(R.id.action_dest_measure_result_to_dest_measure_target)
            }
        }
    }

    private fun setText() {
        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.client.name)
        if (args.isRequiredBodyComp) {
            binding.tvMeasureItem.text = getString(HealthDataType.BODY_COMPOSITION.resId)
        } else {
            binding.tvMeasureItem.text = getString(fitrusViewModel.measureType.resId)
        }
    }

    private fun getResultData(healthDataResponse: HealthDataResponse) {
        when (healthDataResponse) {
            is BloodPressureResponse -> {
                viewModel.getBloodPressureResult(
                    healthDataResponse.bloodId
                )
            }

            is BodyCompositionResponse -> {
                viewModel.getBodyCompResult(
                    healthDataResponse.bodyId
                )
            }

            is HeartRateResponse -> {
                viewModel.getHeartRateResult(
                    healthDataResponse.heartRateId
                )
            }

            is RequiredDataResponse -> {
                if (args.isRequiredBodyComp) {
                    viewModel.getBodyCompResult(
                        healthDataResponse.bodyId
                    )
                } else {
                    viewModel.getBloodPressureResult(
                        healthDataResponse.bloodId
                    )
                }
            }

            is StressResponse -> {
                viewModel.getStressResult(
                    healthDataResponse.stressId
                )
            }

            is TemperatureResponse -> {
                viewModel.getTemperatureResult(
                    healthDataResponse.temperatureId
                )
            }
        }
    }

    private fun observeHealthDataResult() {
        viewModel.result.observe(viewLifecycleOwner) {
            when (it) {
                is BodyCompositionResultResponse -> {
                    setBodyComposition(it)
                }

                is BloodPressureResultResponse -> {
                    setBloodPressure(it)
                }

                is HeartRateResultResponse -> {
                    setHeartRate(it)
                }

                is StressResultResponse -> {
                    setStress(it)
                }

                is TemperatureResultResponse -> {
                    setBodyTemperature(it)
                }
            }
        }
    }

    private fun setBodyComposition(result: BodyCompositionResultResponse) {
        val viewStub = binding.vsResultEight.inflate()
        val resultBinding = LayoutResultEightBinding.bind(viewStub)

        val itemOneBinding = resultBinding.include1
        val itemTwoBinding = resultBinding.include2
        val itemThreeBinding = resultBinding.include3
        val itemFourBinding = resultBinding.include4
        val itemFiveBinding = resultBinding.include5
        val itemSixBinding = resultBinding.include6
        val itemSevenBinding = resultBinding.include7
        val itemEightBinding = resultBinding.include8

        setResultItem(
            itemOneBinding,
            getString(R.string.bfp),
            result.bfp.value.toString(),
            getString(R.string.unit_percent),
            result.bfp.level
        )

        setResultItem(
            itemTwoBinding,
            getString(R.string.bfm),
            result.bfm.value.toString(),
            getString(R.string.unit_kg),
            result.bfm.level
        )

        setResultItem(
            itemThreeBinding,
            getString(R.string.protein),
            result.protein.value.toString(),
            getString(R.string.unit_kg),
            result.protein.level
        )

        setResultItem(
            itemFourBinding,
            getString(R.string.smm),
            result.smm.value.toString(),
            getString(R.string.unit_kg),
            result.smm.level
        )

        setResultItem(
            itemFiveBinding,
            getString(R.string.mineral),
            result.mineral.value.toString(),
            getString(R.string.unit_kg),
            result.mineral.level
        )

        setResultItem(
            itemSixBinding,
            getString(R.string.bmr),
            result.bmr.value.toString(),
            getString(R.string.unit_kcal),
            result.bmr.level
        )

        setResultItem(
            itemSevenBinding,
            getString(R.string.water),
            result.ecf.value.toString(),
            getString(R.string.unit_percent),
            result.ecf.level
        )

        setResultItem(
            itemEightBinding,
            getString(R.string.body_age),
            result.bodyAge.toString(),
            getString(R.string.unit_age),
            ""
        )
    }

    private fun setBloodPressure(result: BloodPressureResultResponse) {
        val viewStub = binding.vsResultTwo.inflate()
        val resultBinding = LayoutResultTwoBinding.bind(viewStub)

        val itemOneBinding = resultBinding.include1
        val itemTwoBinding = resultBinding.include2

        setResultItem(
            itemOneBinding,
            getString(R.string.sbp),
            result.dbp.value.toString(),
            getString(R.string.unit_mmHg),
            result.dbp.level
        )

        setResultItem(
            itemTwoBinding,
            getString(R.string.dbp),
            result.dbp.value.toString(),
            getString(R.string.unit_mmHg),
            result.dbp.level
        )
    }

    private fun setHeartRate(result: HeartRateResultResponse) {
        val viewStub = binding.vsResultTwo.inflate()
        val resultBinding = LayoutResultTwoBinding.bind(viewStub)

        val itemOneBinding = resultBinding.include1
        val itemTwoBinding = resultBinding.include2

        setResultItem(
            itemOneBinding,
            getString(R.string.bpm),
            result.bpm.value.toString(),
            getString(R.string.unit_bpm),
            result.bpm.level
        )

        setResultItem(
            itemTwoBinding,
            getString(R.string.oxygen),
            result.oxygen.value.toString(),
            getString(R.string.unit_percent),
            result.oxygen.level
        )
    }

    private fun setStress(result: StressResultResponse) {
        val viewStub = binding.vsResultTwo.inflate()
        val resultBinding = LayoutResultTwoBinding.bind(viewStub)

        val itemOneBinding = resultBinding.include1
        val itemTwoBinding = resultBinding.include2

        setResultItem(
            itemOneBinding,
            getString(R.string.stress_value),
            result.stressValue.value.toString(),
            "",
            result.stressValue.level
        )

        setResultItem(
            itemTwoBinding,
            getString(R.string.stress_level),
            result.stressLevel,
            "",
            ""
        )
    }

    private fun setBodyTemperature(result: TemperatureResultResponse) {
        val viewStub = binding.vsResultOne.inflate()
        val resultBinding = LayoutResultOneBinding.bind(viewStub)

        val itemOneBinding = resultBinding.include1

        setResultItem(
            itemOneBinding,
            getString(R.string.temp),
            result.temperature.value.toString(),
            getString(R.string.unit_celsius),
            result.temperature.level
        )
    }

    private fun setResultItem(
        itemBinding: LayoutResultItemBinding,
        label: String,
        value: String,
        unit: String,
        grade: String
    ) {
        itemBinding.tvLabel.text = label
        itemBinding.tvValue.text = value
        itemBinding.tvUnit.text = unit
        setGrade(grade, itemBinding.tvGrade)
    }

    private fun setGrade(grade: String, gradeText: TextView) {
        when (grade) {
            getString(GradeType.HIGH.stringResId) -> {
                gradeText.text = getString(GradeType.HIGH.stringResId)
                gradeText.setBackgroundResource(GradeType.HIGH.drawableResId)
                gradeText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        GradeType.HIGH.colorResId
                    )
                )
            }

            getString(GradeType.MIDDLE.stringResId) -> {
                gradeText.text = getString(GradeType.MIDDLE.stringResId)
                gradeText.setBackgroundResource(GradeType.MIDDLE.drawableResId)
                gradeText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        GradeType.MIDDLE.colorResId
                    )
                )
            }

            getString(GradeType.LOW.stringResId) -> {
                gradeText.text = getString(GradeType.LOW.stringResId)
                gradeText.setBackgroundResource(GradeType.LOW.drawableResId)
                gradeText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        GradeType.LOW.colorResId
                    )
                )
            }

            else -> {
                gradeText.visibility = View.INVISIBLE
            }
        }
    }
}