package com.kxnst.bugsgame.presentation.register

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter

import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController

import com.google.android.material.radiobutton.MaterialRadioButton
import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.FragmentRegisterBinding
import com.kxnst.bugsgame.presentation.navigation.navigateToHome
import com.kxnst.bugsgame.presentation.user.UserDialogHelper
import com.kxnst.bugsgame.presentation.user.UserListState
import com.kxnst.bugsgame.presentation.user.UserSessionViewModel

import java.time.LocalDate

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by activityViewModel()
    private val userSessionViewModel: UserSessionViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRegisterBinding.bind(view)

        setupViews()
        observeState()
        observeRegistrationEvents()
    }

    private fun setupViews() {
        binding.actvCourse.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.item_dropdown,
                resources.getStringArray(R.array.register_courses)
            )
        )

        binding.actvCourse.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateCourse(resources.getStringArray(R.array.register_courses)[position])
        }

        binding.etFullName.doAfterTextChanged { value ->
            binding.tilFullName.error = null
            viewModel.updateFullName(value.toString())
        }

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                binding.tvGender.error = null
                viewModel.updateGender(
                    binding.root.findViewById<MaterialRadioButton>(checkedId).text.toString()
                )
            }
        }

        binding.sbDifficulty.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    viewModel.updateDifficulty(progress)
                }

                binding.tvDifficulty.text = getString(
                    R.string.register_difficulty_value,
                    progress
                )
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
        })

        binding.cvBirthDate.maxDate = System.currentTimeMillis()
        binding.cvBirthDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            binding.tvBirthDate.error = null
            viewModel.updateBirthDate(LocalDate.of(year, month + 1, dayOfMonth))
        }

        viewModel.formState.value.birthDate?.let { date ->
            binding.cvBirthDate.date = date.toEpochDay() * MILLIS_PER_DAY
        } ?: viewModel.updateBirthDate(LocalDate.now())

        binding.bSubmit.setOnClickListener { submit() }
        binding.bChooseUser.setOnClickListener { showUserSelection() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formState.collect { state ->
                    binding.tvDifficulty.text = getString(
                        R.string.register_difficulty_value,
                        state.difficulty
                    )

                    state.zodiac?.let { zodiac ->
                        binding.tvSelectedZodiac.text = zodiac.name
                        binding.ivSelectedZodiac.contentDescription = getString(
                            R.string.home_cd_zodiac,
                            zodiac.name
                        )
                        binding.ivSelectedZodiac.setImageResource(
                            zodiac.iconResourceId.takeIf { it != 0 }
                                ?: R.drawable.ic_zodiac_placeholder
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.zodiacLoadState.collect { state ->
                    renderZodiacState(state)
                }
            }
        }
    }

    private fun observeRegistrationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationEvents.collect { event ->
                    when (event) {
                        is RegistrationEvent.Success -> {
                            userSessionViewModel.setCurrentUser(event.user)
                            findNavController().navigateToHome()
                        }

                        RegistrationEvent.DuplicateName -> {
                            binding.tilFullName.error = getString(
                                R.string.register_error_name_exists
                            )
                        }
                    }
                }
            }
        }
    }

    private fun renderZodiacState(state: ZodiacLoadState) {
        binding.pbZodiac.isVisible = state is ZodiacLoadState.Loading
        binding.tvZodiacState.isVisible =
            state is ZodiacLoadState.Empty || state is ZodiacLoadState.Error

        when (state) {
            ZodiacLoadState.Loading -> Unit

            ZodiacLoadState.Empty -> {
                binding.tvZodiacState.text = getString(R.string.register_empty_zodiac)
            }

            is ZodiacLoadState.Error -> {
                binding.tvZodiacState.text = getString(
                    R.string.register_error_zodiac_load
                )
            }

            ZodiacLoadState.Ready -> {
                binding.tvZodiacState.isVisible = false
            }
        }
    }

    private fun submit() {
        val state = viewModel.formState.value

        binding.tilFullName.error = null
        binding.tvGender.error = null
        binding.tilCourse.error = null

        var valid = true

        if (state.fullName.isBlank()) {
            binding.tilFullName.error = getString(R.string.register_error_full_name)
            valid = false
        }

        if (state.gender == null) {
            binding.tvGender.error = getString(R.string.register_error_gender)
            valid = false
        }

        if (state.course == null) {
            binding.tilCourse.error = getString(R.string.register_error_course)
            valid = false
        }

        if (state.birthDate == null) {
            binding.tvBirthDate.error = getString(R.string.register_error_birth_date)
            valid = false
        } else {
            binding.tvBirthDate.error = null
        }

        if (valid) {
            viewModel.register()
        }
    }

    private fun showUserSelection() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val state = userSessionViewModel.usersState.first { it !is UserListState.Loading }) {
                UserListState.Empty -> {
                    UserDialogHelper.showUserSelectionDialog(this@RegisterFragment, emptyList()) { }
                }

                is UserListState.Error -> {
                    UserDialogHelper.showUserSelectionDialog(this@RegisterFragment, emptyList()) { }
                }

                is UserListState.Content -> {
                    UserDialogHelper.showUserSelectionDialog(this@RegisterFragment, state.users) { user ->
                        viewModel.loadUser(user)
                        userSessionViewModel.setCurrentUser(user)
                        findNavController().navigateToHome()
                    }
                }

                UserListState.Loading -> Unit
            }
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
