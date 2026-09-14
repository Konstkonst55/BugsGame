package com.kxnst.bugsgame

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

import com.kxnst.bugsgame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            val navHostFragment =
                supportFragmentManager.findFragmentById(binding.fcvNavHost.id) as NavHostFragment
            val navController = navHostFragment.navController

            binding.bnvMain.setupWithNavController(navController)
            binding.tbMain.inflateMenu(R.menu.menu_toolbar)

            binding.tbMain.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_toolbar_settings) {
                    navController.navigate(R.id.action_global_settings)
                    true
                } else {
                    false
                }
            }

            navController.addOnDestinationChangedListener { controller, destination, _ ->
                val isSettings = destination.id == R.id.settingsFragment
                val isBottomDestination = destination.id in setOf(
                    R.id.homeFragment,
                    R.id.rulesFragment,
                    R.id.authorsFragment
                )

                binding.tbMain.title = destination.label
                binding.bnvMain.isVisible = isBottomDestination
                binding.tbMain.menu.findItem(R.id.action_toolbar_settings).isVisible = isBottomDestination

                if (isSettings) {
                    binding.tbMain.navigationIcon = AppCompatResources.getDrawable(
                        this,
                        androidx.appcompat.R.drawable.abc_ic_ab_back_material
                    )
                    binding.tbMain.setNavigationOnClickListener {
                        controller.navigateUp()
                    }
                } else {
                    binding.tbMain.navigationIcon = null
                    binding.tbMain.setNavigationOnClickListener(null)
                }

            }
        }
    }
}
