package com.example.myapplication.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcv) as NavHostFragment
        navController = navHostFragment.navController
        binding.bnv.setupWithNavController(navController)

        hideBottomNavigationView(navController)
    }

    private fun hideBottomNavigationView(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bnv.visibility = when (destination.id) {
                R.id.dest_schedule -> View.VISIBLE
                R.id.dest_care_list -> View.VISIBLE
                R.id.dest_measurement_target -> View.VISIBLE
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