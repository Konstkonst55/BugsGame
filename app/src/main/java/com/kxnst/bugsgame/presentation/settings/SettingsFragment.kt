package com.kxnst.bugsgame.presentation.settings

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController

import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.data.settings.GameSettingConstraints
import com.kxnst.bugsgame.databinding.FragmentSettingsBinding
import com.kxnst.bugsgame.presentation.navigation.navigateToHome
import com.kxnst.bugsgame.presentation.register.PlayerViewModel
import com.kxnst.bugsgame.presentation.user.UserDialogHelper
import com.kxnst.bugsgame.presentation.user.UserListState
import com.kxnst.bugsgame.presentation.user.UserSessionViewModel

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameSettingsViewModel by viewModel()
    private val userSessionViewModel: UserSessionViewModel by activityViewModel()
    private val playerViewModel: PlayerViewModel by activityViewModel()
    private var isUpdatingFromState = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSettingsBinding.bind(view)

        setupDropdowns()
        setupUserSwitch()
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

    private fun setupUserSwitch() {
        binding.bChangeUser.setOnClickListener {
            UserDialogHelper.showUserChoiceDialog(
                fragment = this,
                onRegisterNew = {
                    playerViewModel.resetForm()
                    findNavController().navigate(R.id.registerFragment)
                },
                onSelectExisting = {
                    showUserSelection()
                }
            )
        }
    }

    private fun showUserSelection() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val state = userSessionViewModel.usersState.first { it !is UserListState.Loading }) {
                UserListState.Empty -> {
                    UserDialogHelper.showUserSelectionDialog(this@SettingsFragment, emptyList()) { }
                }

                is UserListState.Error -> {
                    UserDialogHelper.showUserSelectionDialog(this@SettingsFragment, emptyList()) { }
                }

                is UserListState.Content -> {
                    UserDialogHelper.showUserSelectionDialog(this@SettingsFragment, state.users) { user ->
                        playerViewModel.loadUser(user)
                        userSessionViewModel.setCurrentUser(user)
                        findNavController().navigateToHome()
                    }
                }

                UserListState.Loading -> Unit
            }
        }
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
        _binding = null

        super.onDestroyView()
    }
}
