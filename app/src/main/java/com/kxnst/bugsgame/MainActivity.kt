package com.kxnst.bugsgame

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

import com.kxnst.bugsgame.databinding.ActivityMainBinding
import com.kxnst.bugsgame.presentation.game.GameViewModel

import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModel()

    private val fullscreenBackCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            exitGameFullscreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, fullscreenBackCallback)

        binding.root.post {
            val navHostFragment =
                supportFragmentManager.findFragmentById(binding.fcvNavHost.id) as NavHostFragment
            val navController = navHostFragment.navController

            binding.bnvMain.setupWithNavController(navController)
            binding.tbMain.inflateMenu(R.menu.menu_toolbar)

            binding.tbMain.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_toolbar_settings -> {
                        navController.navigate(R.id.action_global_settings)
                        true
                    }

                    R.id.action_toolbar_restart -> {
                        gameViewModel.requestRestart()
                        true
                    }

                    else -> false
                }
            }

            navController.addOnDestinationChangedListener { controller, destination, _ ->
                if (destination.id != R.id.gameFragment) {
                    exitGameFullscreen()
                }

                updateNavigationUi(controller, destination.id, destination.label)
            }
        }
    }

    fun enterGameFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    fun exitGameFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun updateNavigationUi(
        controller: androidx.navigation.NavController,
        destinationId: Int,
        destinationLabel: CharSequence?
    ) {
        val isGame = destinationId == R.id.gameFragment
        val isSettings = destinationId == R.id.settingsFragment
        val isBottomDestination = destinationId in setOf(
            R.id.homeFragment,
            R.id.gameFragment,
            R.id.rulesFragment,
            R.id.authorsFragment
        )
        val isFullscreen =
            isGame && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        binding.tbMain.title = destinationLabel
        binding.ablMain.isVisible = !isFullscreen
        binding.bnvMain.isVisible = isBottomDestination && !isFullscreen

        binding.tbMain.menu.findItem(R.id.action_toolbar_settings).isVisible =
            isBottomDestination && !isFullscreen
        binding.tbMain.menu.findItem(R.id.action_toolbar_restart).isVisible =
            isGame && !isFullscreen

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

        fullscreenBackCallback.isEnabled = isFullscreen
        updateSystemBars(isFullscreen)
    }

    private fun updateSystemBars(isFullscreen: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, !isFullscreen)

        val controller = WindowInsetsControllerCompat(window, window.decorView)

        if (isFullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
