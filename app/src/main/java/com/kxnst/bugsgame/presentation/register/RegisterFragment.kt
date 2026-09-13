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
import java.time.LocalDate
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRegisterBinding.bind(view)

        setupViews()
        observeState()
    }

    private fun setupViews() {
        binding.actvCourse.setAdapter(
            ArrayAdapter(
                requireContext(),
                com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                resources.getStringArray(R.array.register_courses)
            )
        )

        binding.actvCourse.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateCourse(resources.getStringArray(R.array.register_courses)[position])
        }

        binding.etFullName.doAfterTextChanged { value ->
            viewModel.updateFullName(value.toString())
        }

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
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

                binding.tvDifficulty.text = getString(R.string.register_difficulty_value, progress)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
        })

        binding.cvBirthDate.maxDate = System.currentTimeMillis()
        binding.cvBirthDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.updateBirthDate(LocalDate.of(year, month + 1, dayOfMonth))
        }

        viewModel.formState.value.birthDate?.let { date ->
            binding.cvBirthDate.date = date.toEpochDay() * 86_400_000L
        } ?: viewModel.updateBirthDate(LocalDate.now())

        binding.bSubmit.setOnClickListener { submit() }
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

                        val resourceId = requireContext().resources.getIdentifier(
                            zodiac.iconResourceName,
                            "drawable",
                            requireContext().packageName
                        ).takeIf { it != 0 } ?: R.drawable.ic_zodiac_placeholder

                        binding.ivSelectedZodiac.setImageResource(resourceId)
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

    private fun renderZodiacState(state: ZodiacLoadState) {
        binding.pbZodiac.isVisible = state is ZodiacLoadState.Loading
        binding.tvZodiacState.isVisible = state is ZodiacLoadState.Empty || state is ZodiacLoadState.Error

        when (state) {
            ZodiacLoadState.Loading -> Unit

            ZodiacLoadState.Empty -> {
                binding.tvZodiacState.text = getString(R.string.register_empty_zodiac)
            }

            is ZodiacLoadState.Error -> {
                binding.tvZodiacState.text = getString(R.string.register_error_zodiac_load)
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

        if (valid && viewModel.register()) {
            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
