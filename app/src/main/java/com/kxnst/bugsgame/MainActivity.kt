package com.kxnst.bugsgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
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

            navController.addOnDestinationChangedListener { _, destination, _ ->
                binding.tbMain.title = destination.label

                binding.tbMain.navigationIcon = null
                binding.tbMain.setNavigationOnClickListener(null)
            }
        }
    }
}
