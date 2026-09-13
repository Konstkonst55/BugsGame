package com.kxnst.bugsgame.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxnst.bugsgame.data.player.PlayerRegistration
import com.kxnst.bugsgame.data.zodiac.ZodiacRepository
import com.kxnst.bugsgame.data.zodiac.ZodiacSign
import com.kxnst.bugsgame.domain.zodiac.ZodiacCalculator
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerFormState(
    val fullName: String = "",
    val gender: String? = null,
    val course: String? = null,
    val difficulty: Int = 2,
    val birthDate: LocalDate? = null,
    val zodiac: ZodiacSign? = null,
    val registration: PlayerRegistration? = null
)

sealed interface ZodiacLoadState {
    data object Loading : ZodiacLoadState
    data object Ready : ZodiacLoadState
    data object Empty : ZodiacLoadState
    data class Error(val message: String) : ZodiacLoadState
}

class PlayerViewModel(
    private val zodiacRepository: ZodiacRepository,
    private val zodiacCalculator: ZodiacCalculator
) : ViewModel() {
    private val _formState = MutableStateFlow(PlayerFormState())
    val formState: StateFlow<PlayerFormState> = _formState.asStateFlow()

    private val _zodiacLoadState = MutableStateFlow<ZodiacLoadState>(ZodiacLoadState.Loading)
    val zodiacLoadState: StateFlow<ZodiacLoadState> = _zodiacLoadState.asStateFlow()

    private var zodiacSigns: List<ZodiacSign> = emptyList()

    init {
        loadZodiacSigns()
    }

    fun updateFullName(value: String) {
        _formState.value = _formState.value.copy(fullName = value)
    }

    fun updateGender(value: String) {
        _formState.value = _formState.value.copy(gender = value)
    }

    fun updateCourse(value: String) {
        _formState.value = _formState.value.copy(course = value)
    }

    fun updateDifficulty(value: Int) {
        _formState.value = _formState.value.copy(difficulty = value)
    }

    fun updateBirthDate(value: LocalDate) {
        val zodiac = zodiacCalculator.calculate(value, zodiacSigns)

        _formState.value = _formState.value.copy(
            birthDate = value,
            zodiac = zodiac
        )
    }

    fun register(): Boolean {
        val state = _formState.value
        val zodiac = state.zodiac ?: return false
        val gender = state.gender ?: return false
        val course = state.course ?: return false

        if (state.fullName.isBlank() || state.birthDate == null) {
            return false
        }

        _formState.value = state.copy(
            registration = PlayerRegistration(
                fullName = state.fullName.trim(),
                gender = gender,
                course = course,
                difficulty = state.difficulty,
                birthDate = state.birthDate.toString(),
                zodiacName = zodiac.name
            )
        )

        return true
    }

    fun clearRegistration() {
        _formState.value = _formState.value.copy(registration = null)
    }

    private fun loadZodiacSigns() {
        viewModelScope.launch {
            _zodiacLoadState.value = ZodiacLoadState.Loading

            runCatching { zodiacRepository.getSigns() }
                .onSuccess { signs ->
                    zodiacSigns = signs
                    _zodiacLoadState.value = if (signs.isEmpty()) {
                        ZodiacLoadState.Empty
                    } else {
                        ZodiacLoadState.Ready
                    }

                    _formState.value.birthDate?.let(::updateBirthDate)
                }
                .onFailure { error ->
                    _zodiacLoadState.value = ZodiacLoadState.Error(error.message.orEmpty())
                }
        }
    }
}
