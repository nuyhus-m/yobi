package com.example.myapplication.ui.measure.measureloading

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.base.HealthDataType
import com.example.myapplication.data.dto.model.BloodPressureResult
import com.example.myapplication.data.dto.model.BodyCompositionResult
import com.example.myapplication.data.dto.model.HeartRateResult
import com.example.myapplication.data.dto.model.StressResult
import com.example.myapplication.data.dto.model.TemperatureResult
import com.example.myapplication.data.dto.request.measure.BloodPressureRequest
import com.example.myapplication.data.dto.request.measure.BodyCompositionRequest
import com.example.myapplication.data.dto.request.measure.HeartRateRequest
import com.example.myapplication.data.dto.request.measure.RequiredDataRequest
import com.example.myapplication.data.dto.request.measure.StressRequest
import com.example.myapplication.data.dto.request.measure.TemperatureRequest
import com.example.myapplication.databinding.FragmentMeasureLoadingBinding
import com.example.myapplication.ui.FitrusViewModel
import com.example.myapplication.ui.measure.measureloading.viewmodel.MeasureLoadingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "MeasureLoadingFragment"

@AndroidEntryPoint
class MeasureLoadingFragment : BaseFragment<FragmentMeasureLoadingBinding>(
    FragmentMeasureLoadingBinding::bind,
    R.layout.fragment_measure_loading
) {

    private val fitrusViewModel by activityViewModels<FitrusViewModel>()
    private val viewModel by viewModels<MeasureLoadingViewModel>()
    private var backPressedTime = 0L
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime < 2000) {
                        toast?.cancel()
                        isEnabled = false
                        showToast("기기를 한 번 껐다가 다시 켠 후, 측정을 다시 시도해 주세요.")
                        fitrusViewModel.disconnectDevice()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        backPressedTime = currentTime
                        toast =
                            Toast.makeText(
                                requireContext(),
                                "한 번 더 누르면 측정이 중지됩니다.",
                                Toast.LENGTH_SHORT
                            )
                        toast?.show()
                    }
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle()
        initView()
        startMeasure()
        observeMeasureResult()
        observeHealthDataResponse()
        observeConnectState()
        observeToastMessage()
        fitrusViewModel.setMeasuringStatus(true)
        fitrusViewModel.setIsFirst(true)
    }

    private fun setTitle() {
        binding.tvTitle.text = getString(R.string.measure_title, fitrusViewModel.client.name)
    }

    private fun initView() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val animationDrawable = binding.ivCharacter.background as AnimationDrawable
                animationDrawable.start()
            }
        }
    }

    private fun startMeasure() {
        fitrusViewModel.startMeasure()
    }

    private fun observeMeasureResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fitrusViewModel.measureResult.collect { result ->
                    when (result) {
                        is BodyCompositionResult -> {
                            if (fitrusViewModel.isMeasured) {
                                viewModel.saveBodyCompositionData(
                                    fitrusViewModel.client.clientId,
                                    result.toRequest()
                                )
                            } else {
                                fitrusViewModel.setBodyCompositionResult(result)
                                findNavController().navigate(R.id.action_dest_measure_loading_to_dest_measure_transition)
                            }
                        }

                        is BloodPressureResult -> {
                            if (fitrusViewModel.isMeasured) {
                                viewModel.saveBloodPressureData(
                                    fitrusViewModel.client.clientId,
                                    result.toRequest()
                                )
                            } else {
                                viewModel.saveRequiredMeasureData(
                                    fitrusViewModel.client.clientId,
                                    RequiredDataRequest(
                                        bodyCompositionRequest = fitrusViewModel.bodyCompositionResult.toRequest(),
                                        bloodPressureRequest = result.toRequest()
                                    )
                                )
                            }
                        }

                        is HeartRateResult -> {
                            viewModel.saveHeartRateData(
                                fitrusViewModel.client.clientId,
                                result.toRequest()
                            )
                        }

                        is StressResult -> {
                            viewModel.saveStressData(
                                fitrusViewModel.client.clientId,
                                result.toRequest()
                            )
                        }

                        is TemperatureResult -> {
                            viewModel.saveTemperatureData(
                                fitrusViewModel.client.clientId,
                                result.toRequest()
                            )
                        }
                    }

                    fitrusViewModel.setMeasuringStatus(false)
                }
            }
        }
    }

    private fun observeHealthDataResponse() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.healthDataResponse.collect {
                    fitrusViewModel.setHealthDataResponse(it)
                    if (!fitrusViewModel.isMeasured && fitrusViewModel.measureType == HealthDataType.BLOOD_PRESSURE) {
                        val action =
                            MeasureLoadingFragmentDirections.actionDestMeasureLoadingToDestMeasureResult(
                                true
                            )
                        findNavController().navigate(action)

                    } else {
                        findNavController().navigate(R.id.action_dest_measure_loading_to_dest_measure_result)
                    }
                }
            }
        }
    }

    private fun observeConnectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fitrusViewModel.isConnected.collect {
                    if (!it && fitrusViewModel.isFirst) {
                        showToast("연결이 끊겼습니다. 다시 시도해주세요😭😭")
                        findNavController().navigate(R.id.action_dest_measure_loading_to_dest_device_connect)
                    }
                }
            }
        }
    }

    private fun observeToastMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fitrusViewModel.toastMessage.collect {
                    showToast(it)
                }
            }
        }
    }

    private fun BodyCompositionResult.toRequest(): BodyCompositionRequest {
        val totalWater = ecw + icw
        val ecf = if (totalWater != 0f) (ecw / totalWater) * 100 else 0f

        return BodyCompositionRequest(
            bfm = bfm,
            bfp = bfp,
            bmr = bmr,
            bodyAge = bodyAge,
            ecf = ecf,
            ecw = ecw,
            icw = icw,
            mineral = mineral,
            protein = protein,
            smm = smm
        )
    }

    private fun BloodPressureResult.toRequest(): BloodPressureRequest {
        return BloodPressureRequest(
            dbp = DBP,
            sbp = SBP
        )
    }

    private fun HeartRateResult.toRequest(): HeartRateRequest {
        return HeartRateRequest(
            bpm = this.bpm,
            oxygen = this.oxygen
        )
    }

    private fun StressResult.toRequest(): StressRequest {
        return StressRequest(
            bpm = bpm,
            oxygen = oxygen,
            stressLevel = level,
            stressValue = value
        )
    }

    private fun TemperatureResult.toRequest(): TemperatureRequest {
        return TemperatureRequest(
            temperature = temp
        )
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        fitrusViewModel.setIsFirst(false)
    }
}