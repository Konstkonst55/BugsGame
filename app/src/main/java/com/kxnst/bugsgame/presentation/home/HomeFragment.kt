package com.kxnst.bugsgame.presentation.home

import android.os.Bundle
import android.view.View

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.FragmentHomeBinding
import com.kxnst.bugsgame.presentation.register.PlayerViewModel
import com.kxnst.bugsgame.domain.game.GameRules
import com.kxnst.bugsgame.presentation.user.UserSessionViewModel

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val userSessionViewModel: UserSessionViewModel by activityViewModel()
    private val playerViewModel: PlayerViewModel by activityViewModel()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userSessionViewModel.currentUser.collect { user ->
                        binding.tvEmptyHome.isVisible = user == null

                        if (user == null) {
                            return@collect
                        }

                        binding.tvFullName.text = user.name
                        binding.tvBirthDate.text = LocalDate.parse(user.birthDate).format(dateFormatter)
                        binding.tvCourse.text = user.course

                        val stars = when (user.difficulty.coerceIn(
                            GameRules.MIN_DIFFICULTY,
                            GameRules.MAX_DIFFICULTY
                        )) {
                            GameRules.MIN_DIFFICULTY -> getString(
                                R.string.home_text_difficulty_stars_one
                            )
                            GameRules.DEFAULT_DIFFICULTY -> getString(
                                R.string.home_text_difficulty_stars_two
                            )
                            else -> getString(
                                R.string.home_text_difficulty_stars_three
                            )
                        }
                        binding.tvDifficulty.text = getString(
                            R.string.home_difficulty_value,
                            stars
                        )

                        binding.tvBestScore.text = user.bestScore.toString()
                        binding.tvBestScore.contentDescription = getString(
                            R.string.home_cd_best_score,
                            user.bestScore
                        )

                        val isMale = user.gender == getString(R.string.register_gender_male)
                        binding.ivGender.setImageResource(
                            if (isMale) R.drawable.ic_man else R.drawable.ic_woman
                        )
                        binding.ivGender.contentDescription = getString(
                            if (isMale) R.string.home_cd_gender_male else R.string.home_cd_gender_female
                        )
                        binding.flGender.setBackgroundResource(
                            if (isMale) R.drawable.bg_gender_male else R.drawable.bg_gender_female
                        )
                    }
                }

                launch {
                    playerViewModel.formState.collect { state ->
                        state.zodiac?.let { zodiac ->
                            binding.ivZodiac.setImageResource(
                                zodiac.iconResourceId.takeIf { it != 0 } ?: R.drawable.ic_zodiac_placeholder
                            )
                            binding.ivZodiac.contentDescription = getString(
                                R.string.home_cd_zodiac,
                                zodiac.name
                            )
                            binding.tvZodiacName.text = zodiac.name
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }
}
