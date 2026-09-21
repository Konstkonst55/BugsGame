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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by activityViewModel()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formState.collect { state ->
                    val registration = state.registration
                    binding.tvEmptyHome.isVisible = registration == null
                    if (registration == null) return@collect

                    binding.tvFullName.text = getString(R.string.home_text_full_name, registration.fullName)
                    binding.tvGender.text = getString(R.string.home_text_gender, registration.gender)
                    binding.tvCourse.text = getString(R.string.home_text_course, registration.course)
                    binding.tvDifficulty.text = getString(R.string.home_text_difficulty, registration.difficulty)
                    val birthDate = LocalDate.parse(registration.birthDate)
                    binding.tvBirthDate.text = getString(
                        R.string.home_text_birth_date,
                        birthDate.format(dateFormatter)
                    )
                    binding.tvZodiacName.text = registration.zodiacName

                    state.zodiac?.let { zodiac ->
                        binding.ivZodiac.setImageResource(
                            zodiac.iconResourceId.takeIf { it != 0 }
                                ?: R.drawable.ic_zodiac_placeholder
                        )
                        binding.ivZodiac.contentDescription = getString(
                            R.string.home_cd_zodiac,
                            zodiac.name
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
