package com.kxnst.bugsgame.presentation.game

import android.content.res.Configuration
import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController

import com.kxnst.bugsgame.MainActivity
import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.FragmentGameBinding
import com.kxnst.bugsgame.presentation.user.UserSessionViewModel

import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.activityViewModel

class GameFragment : Fragment(R.layout.fragment_game) {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModel()
    private val userSessionViewModel: UserSessionViewModel by activityViewModel()
    private var startDialogShown = false
    private var lastResultRoundId: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGameBinding.bind(view)
        binding.gbvGame.onTap = viewModel::handleTap
        binding.ibFullscreen.setOnClickListener { toggleFullscreen() }

        userSessionViewModel.currentUser.value?.let { user ->
            viewModel.ensureUser(user.name)
        }

        observeState()
        observeRestartRequests()
        observeCurrentUser()
        applyFullscreenLayout(
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        )
    }

    override fun onStart() {
        super.onStart()

        viewModel.resumeRound()
    }

    override fun onStop() {
        viewModel.pauseGame()

        super.onStop()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                    handlePhase(state)
                }
            }
        }
    }

    private fun observeRestartRequests() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.restartRequests.collect {
                    showRestartDialog()
                }
            }
        }
    }

    private fun observeCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userSessionViewModel.currentUser.collect { user ->
                    if (user != null) {
                        viewModel.ensureUser(user.name)
                    }
                }
            }
        }
    }

    private fun renderState(state: GameState) {
        binding.tvScore.text = state.score.toString()
        binding.tvPenalties.text = state.penalties.toString()
        binding.tvTimer.text = formatTime(state.remainingSeconds)

        binding.tvFullscreenScore.text = state.score.toString()
        binding.tvFullscreenPenalties.text = state.penalties.toString()
        binding.tvFullscreenTimer.text = formatTime(state.remainingSeconds)
        binding.tvFullscreenScore.contentDescription = getString(
            R.string.game_score_cd,
            state.score
        )
        binding.tvFullscreenPenalties.contentDescription = getString(
            R.string.game_penalty_cd,
            state.penalties
        )
        binding.tvFullscreenTimer.contentDescription = getString(
            R.string.game_timer_cd,
            state.remainingSeconds / SECONDS_PER_MINUTE,
            state.remainingSeconds % SECONDS_PER_MINUTE
        )

        binding.gbvGame.render(state)
    }

    private fun handlePhase(state: GameState) {
        when (state.phase) {
            GamePhase.READY -> {
                if (!startDialogShown) {
                    startDialogShown = true
                    showStartDialog()
                }
            }

            GamePhase.RUNNING -> {
                startDialogShown = false
            }

            GamePhase.FINISHED -> {
                val result = state.result ?: return

                if (lastResultRoundId != result.roundId) {
                    lastResultRoundId = result.roundId
                    showResultDialog(result)
                }
            }
        }
    }

    private fun showStartDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.game_start_title)
            .setPositiveButton(R.string.game_start_button) { _, _ ->
                userSessionViewModel.currentUser.value?.let(viewModel::startRound)
            }
            .setNegativeButton(R.string.game_rules_button) { _, _ ->
                findNavController().navigate(R.id.action_global_rules)
            }
            .setCancelable(false)
            .show()
    }

    private fun showRestartDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.game_restart_title)
            .setMessage(R.string.game_restart_message)
            .setPositiveButton(R.string.game_yes) { _, _ ->
                startRoundForCurrentUser()
            }
            .setNegativeButton(R.string.game_no) { _, _ ->
                viewModel.resumeRound()
            }
            .setOnCancelListener {
                viewModel.resumeRound()
            }
            .show()
    }

    private fun showResultDialog(result: GameResult) {
        val message = getString(
            R.string.game_result_message,
            result.rawScore,
            result.penalties,
            result.finalScore,
            result.roundDurationSeconds
        )

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.game_result_title)
            .setMessage(message)
            .setPositiveButton(R.string.game_result_restart) { _, _ ->
                startRoundForCurrentUser()
            }
            .setNegativeButton(R.string.game_result_home) { _, _ ->
                findNavController().navigate(
                    R.id.homeFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.gameFragment, true)
                        .build()
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun startRoundForCurrentUser() {
        val user = userSessionViewModel.currentUser.value ?: return
        viewModel.startRound(user)
    }

    private fun toggleFullscreen() {
        val activity = activity as? MainActivity ?: return
        val isFullscreen =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isFullscreen) {
            activity.exitGameFullscreen()
        } else {
            activity.enterGameFullscreen()
        }
    }

    private fun applyFullscreenLayout(isFullscreen: Boolean) {
        binding.cvScore.isVisible = !isFullscreen
        binding.cvPenalties.isVisible = !isFullscreen
        binding.cvTimer.isVisible = !isFullscreen
        binding.clFullscreenHud.isVisible = isFullscreen

        binding.ibFullscreen.setImageResource(
            if (isFullscreen) {
                R.drawable.ic_game_fullscreen_exit
            } else {
                R.drawable.ic_game_fullscreen
            }
        )
        binding.ibFullscreen.contentDescription = getString(
            if (isFullscreen) {
                R.string.game_cd_exit_fullscreen
            } else {
                R.string.game_cd_enter_fullscreen
            }
        )

        val padding = if (isFullscreen) {
            resources.getDimensionPixelSize(R.dimen.size_zero)
        } else {
            resources.getDimensionPixelSize(R.dimen.padding_medium)
        }

        binding.root.setPadding(padding, padding, padding, padding)

        val boardLayoutParams = binding.gbvGame.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        boardLayoutParams.topMargin = resources.getDimensionPixelSize(
            if (isFullscreen) R.dimen.size_zero else R.dimen.margin_medium
        )
        binding.gbvGame.layoutParams = boardLayoutParams
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE

        return getString(R.string.game_timer_value, minutes, seconds)
    }

    override fun onDestroyView() {
        binding.gbvGame.onTap = null
        _binding = null

        super.onDestroyView()
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60
    }
}
