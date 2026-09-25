package com.kxnst.bugsgame.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.kxnst.bugsgame.data.player.PlayerRegistration
import com.kxnst.bugsgame.data.user.UserRegistrationResult
import com.kxnst.bugsgame.data.user.UserRepository
import com.kxnst.bugsgame.data.zodiac.ZodiacRepository
import com.kxnst.bugsgame.data.zodiac.ZodiacSign
import com.kxnst.bugsgame.domain.user.UserProfile
import com.kxnst.bugsgame.domain.game.GameRules
import com.kxnst.bugsgame.domain.zodiac.ZodiacCalculator

import java.time.LocalDate

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val zodiacRepository: ZodiacRepository,
    private val zodiacCalculator: ZodiacCalculator,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _formState = MutableStateFlow(PlayerFormState())
    val formState: StateFlow<PlayerFormState> = _formState.asStateFlow()

    private val _zodiacLoadState = MutableStateFlow<ZodiacLoadState>(ZodiacLoadState.Loading)
    val zodiacLoadState: StateFlow<ZodiacLoadState> = _zodiacLoadState.asStateFlow()

    private val _registrationEvents = MutableSharedFlow<RegistrationEvent>(extraBufferCapacity = 1)
    val registrationEvents: SharedFlow<RegistrationEvent> = _registrationEvents.asSharedFlow()

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

    fun resetForm() {
        _formState.value = PlayerFormState()
    }

    fun loadUser(user: UserProfile) {
        val zodiac = zodiacSigns.firstOrNull { it.name == user.zodiacName }

        _formState.value = PlayerFormState(
            fullName = user.name,
            gender = user.gender,
            course = user.course,
            difficulty = user.difficulty,
            birthDate = LocalDate.parse(user.birthDate),
            zodiac = zodiac,
            registration = PlayerRegistration(
                fullName = user.name,
                gender = user.gender,
                course = user.course,
                difficulty = user.difficulty,
                birthDate = user.birthDate,
                zodiacName = user.zodiacName
            )
        )
    }

    fun register() {
        val state = _formState.value
        val zodiac = state.zodiac ?: return
        val gender = state.gender ?: return
        val course = state.course ?: return
        val birthDate = state.birthDate ?: return

        if (state.fullName.isBlank()) {
            return
        }

        val name = state.fullName.trim()
        val user = UserProfile(
            name = name,
            gender = gender,
            course = course,
            difficulty = state.difficulty,
            birthDate = birthDate.toString(),
            zodiacName = zodiac.name,
            bestScore = 0
        )

        viewModelScope.launch {
            when (userRepository.register(user)) {
                UserRegistrationResult.SUCCESS -> {
                    _formState.value = state.copy(
                        registration = PlayerRegistration(
                            fullName = user.name,
                            gender = user.gender,
                            course = user.course,
                            difficulty = user.difficulty,
                            birthDate = user.birthDate,
                            zodiacName = user.zodiacName
                        )
                    )
                    _registrationEvents.emit(RegistrationEvent.Success(user))
                }

                UserRegistrationResult.DUPLICATE_NAME -> {
                    _registrationEvents.emit(RegistrationEvent.DuplicateName)
                }
            }
        }
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

data class PlayerFormState(
    val fullName: String = "",
    val gender: String? = null,
    val course: String? = null,
    val difficulty: Int = GameRules.DEFAULT_DIFFICULTY,
    val birthDate: LocalDate? = null,
    val zodiac: ZodiacSign? = null,
    val registration: PlayerRegistration? = null
)

sealed interface RegistrationEvent {
    data class Success(val user: UserProfile) : RegistrationEvent
    data object DuplicateName : RegistrationEvent
}

sealed interface ZodiacLoadState {
    data object Loading : ZodiacLoadState
    data object Ready : ZodiacLoadState
    data object Empty : ZodiacLoadState
    data class Error(val message: String) : ZodiacLoadState
}
