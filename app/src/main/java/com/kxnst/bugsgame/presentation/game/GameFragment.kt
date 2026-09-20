package com.kxnst.bugsgame.presentation.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.FragmentGameBinding

import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.activityViewModel

class GameFragment : Fragment(R.layout.fragment_game) {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGameBinding.bind(view)
        binding.gbvGame.onTap = viewModel::handleTap

        observeState()
    }

    override fun onStart() {
        super.onStart()

        viewModel.startGame()
    }

    override fun onStop() {
        viewModel.pauseGame()

        super.onStop()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.tvScore.text = getString(
                        R.string.game_score_value,
                        state.score
                    )
                    binding.tvPenalties.text = getString(
                        R.string.game_penalty_value,
                        state.penalties
                    )
                    binding.gbvGame.render(state)
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.gbvGame.onTap = null
        _binding = null

        super.onDestroyView()
    }
}
