package com.kxnst.bugsgame.presentation.settings

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.data.settings.GameSettingConstraints
import com.kxnst.bugsgame.databinding.FragmentSettingsBinding

import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameSettingsViewModel by viewModel()
    private var isUpdatingFromState = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSettingsBinding.bind(view)

        setupDropdowns()
        observeSettings()
    }

    private fun setupDropdowns() {
        setupDropdown(
            binding.actvSpeed,
            GameSettingConstraints.MIN_SPEED..GameSettingConstraints.MAX_SPEED
        ) { viewModel.updateSpeed(it) }

        setupDropdown(
            binding.actvMaxCockroaches,
            GameSettingConstraints.MIN_MAX_COCKROACHES..
                GameSettingConstraints.MAX_MAX_COCKROACHES
        ) { viewModel.updateMaxCockroaches(it) }

        setupDropdown(
            binding.actvBonusInterval,
            GameSettingConstraints.MIN_BONUS_INTERVAL_SECONDS..
                GameSettingConstraints.MAX_BONUS_INTERVAL_SECONDS
        ) { viewModel.updateBonusIntervalSeconds(it) }

        setupDropdown(
            binding.actvRoundDuration,
            GameSettingConstraints.MIN_ROUND_DURATION_SECONDS..
                GameSettingConstraints.MAX_ROUND_DURATION_SECONDS
        ) { viewModel.updateRoundDurationSeconds(it) }
    }

    private fun setupDropdown(
        view: MaterialAutoCompleteTextView,
        range: IntRange,
        onValueSelected: (Int) -> Unit
    ) {
        view.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.item_dropdown,
                range.map { it.toString() }
            )
        )

        view.setOnItemClickListener { _, _, position, _ ->
            if (!isUpdatingFromState) {
                onValueSelected(range.elementAt(position))
            }
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    isUpdatingFromState = true

                    binding.actvSpeed.setText(settings.speed.toString(), false)
                    binding.actvMaxCockroaches.setText(
                        settings.maxCockroaches.toString(),
                        false
                    )
                    binding.actvBonusInterval.setText(
                        settings.bonusIntervalSeconds.toString(),
                        false
                    )
                    binding.actvRoundDuration.setText(
                        settings.roundDurationSeconds.toString(),
                        false
                    )

                    isUpdatingFromState = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
