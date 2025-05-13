package com.example.myapplication.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.util.setOnSingleClickListener
import com.onesoftdigm.fitrus.device.sdk.FitrusDevice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val mainViewModel: MainViewModel by viewModels()
    private val fitrusViewModel: FitrusViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcv) as NavHostFragment
        navController = navHostFragment.navController
        binding.bnv.setupWithNavController(navController)

        updateBottomNavigationView(navController)
        updateBottomNavCenterBtn(navController)

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(navController.graph.startDestinationId, false)
            .build()

        binding.ivCenter.setOnSingleClickListener {
            navController.navigate(R.id.dest_measure_target)
        }

        binding.bnv.setOnItemSelectedListener { item ->
            if (item.itemId != binding.bnv.selectedItemId) {
                navController.navigate(item.itemId, null, options)
            }
            true
        }
        mainViewModel.fetchClients()

        initFiturusDevice()
    }

    private fun initFiturusDevice() {
        val fitrusDevice = FitrusDevice(
            this,
            fitrusViewModel,
            BuildConfig.FITRUS_API_KEY
        )
        fitrusViewModel.initFitrusDevice(fitrusDevice)
    }

    private fun updateBottomNavigationView(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bnv.visibility = when (destination.id) {
                R.id.dest_schedule -> View.VISIBLE
                R.id.dest_care_list -> View.VISIBLE
                R.id.dest_measure_target -> View.VISIBLE
                R.id.dest_visit_log_list -> View.VISIBLE
                R.id.dest_my_page -> View.VISIBLE
                R.id.dest_schedule_register_dialog -> View.VISIBLE
                R.id.dest_schedule_delete_dialog -> View.VISIBLE
                R.id.dest_logout_dialog -> View.VISIBLE
                R.id.dest_withdrawal_dialog -> View.VISIBLE
                else -> View.GONE
            }
        }
    }

    private fun updateBottomNavCenterBtn(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.ivCenter.isSelected = destination.id == R.id.dest_measure_target

            binding.ivCenter.visibility = when (destination.id) {
                R.id.dest_schedule -> View.VISIBLE
                R.id.dest_care_list -> View.VISIBLE
                R.id.dest_measure_target -> View.VISIBLE
                R.id.dest_visit_log_list -> View.VISIBLE
                R.id.dest_my_page -> View.VISIBLE
                R.id.dest_schedule_register_dialog -> View.VISIBLE
                R.id.dest_schedule_delete_dialog -> View.VISIBLE
                R.id.dest_logout_dialog -> View.VISIBLE
                R.id.dest_withdrawal_dialog -> View.VISIBLE
                else -> View.GONE
            }
        }
    }
}