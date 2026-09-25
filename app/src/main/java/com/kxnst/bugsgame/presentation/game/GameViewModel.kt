package com.kxnst.bugsgame.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.kxnst.bugsgame.data.settings.GameSettings
import com.kxnst.bugsgame.data.settings.GameSettingsRepository
import com.kxnst.bugsgame.data.user.UserRepository
import com.kxnst.bugsgame.domain.game.CalculateRoundScoreUseCase
import com.kxnst.bugsgame.domain.game.RoundScoreInput
import com.kxnst.bugsgame.domain.user.UserProfile

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val settingsRepository: GameSettingsRepository,
    private val userRepository: UserRepository,
    private val calculateRoundScore: CalculateRoundScoreUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _restartRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val restartRequests: SharedFlow<Unit> = _restartRequests.asSharedFlow()

    private val random = Random(System.currentTimeMillis())
    private var gameJob: Job? = null
    private var activeSettings: GameSettings? = null
    private var activeUserName: String? = null
    private var remainingTimeMs = 0L
    private var spawnRemainingMs = 0L
    private var nextBugId = 0L
    private var nextRoundId = 0L
    private var activeDifficulty = DEFAULT_DIFFICULTY

    fun ensureUser(userName: String) {
        if (activeUserName != null && activeUserName != userName) {
            pauseGame()
            activeSettings = null
            _state.value = GameState()
        }

        activeUserName = userName
    }

    fun startRound(user: UserProfile) {
        pauseGame()
        ensureUser(user.name)

        val settings = settingsRepository.settings.value
        activeSettings = settings
        activeDifficulty = user.difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
        remainingTimeMs = settings.roundDurationSeconds * MILLIS_PER_SECOND
        spawnRemainingMs = 0L

        nextRoundId += 1
        _state.value = GameState(
            phase = GamePhase.RUNNING,
            score = 0,
            penalties = 0,
            remainingSeconds = settings.roundDurationSeconds,
            bugs = emptyList(),
            result = null
        )

        startGameLoop()
    }

    fun resumeRound() {
        if (_state.value.phase == GamePhase.RUNNING) {
            startGameLoop()
        }
    }

    fun pauseGame() {
        gameJob?.cancel()
        gameJob = null
    }

    fun requestRestart() {
        pauseGame()
        _restartRequests.tryEmit(Unit)
    }

    fun handleTap(x: Float, y: Float) {
        if (_state.value.phase != GamePhase.RUNNING) {
            return
        }

        val state = _state.value
        val hitIndex = state.bugs.indexOfLast { bug ->
            abs(bug.x - x) <= BUG_HIT_HALF_SIZE &&
                abs(bug.y - y) <= BUG_HIT_HALF_SIZE
        }

        if (hitIndex == -1) {
            _state.value = state.copy(penalties = state.penalties + 1)
            return
        }

        val hitBug = state.bugs[hitIndex]
        val updatedBugs = state.bugs.toMutableList().apply {
            removeAt(hitIndex)
        }

        _state.value = state.copy(
            score = state.score + hitBug.type.points,
            bugs = updatedBugs
        )
    }

    private fun startGameLoop() {
        if (gameJob?.isActive == true) {
            return
        }

        gameJob = viewModelScope.launch {
            runGameLoop()
        }
    }

    private suspend fun runGameLoop() {
        var lastTickAt = System.nanoTime()

        while (currentCoroutineContext().isActive) {
            val currentTickAt = System.nanoTime()
            val deltaSeconds = ((currentTickAt - lastTickAt) / NANOS_PER_SECOND)
                .coerceIn(0f, MAX_DELTA_SECONDS)

            lastTickAt = currentTickAt
            updateGame(deltaSeconds)

            if (_state.value.phase != GamePhase.RUNNING) {
                return
            }

            delay(GAME_TICK_DELAY_MS)
        }
    }

    private fun updateGame(deltaSeconds: Float) {
        val settings = activeSettings ?: return
        val deltaMs = (deltaSeconds * MILLIS_PER_SECOND).toLong()

        remainingTimeMs = (remainingTimeMs - deltaMs).coerceAtLeast(0L)

        if (remainingTimeMs == 0L) {
            finishRound(settings)
            return
        }

        spawnRemainingMs = (spawnRemainingMs - deltaMs).coerceAtLeast(0L)

        val movedBugs = _state.value.bugs.map { bug ->
            moveBug(bug, settings.speed, deltaSeconds)
        }

        val shouldSpawn =
            movedBugs.size < settings.maxCockroaches && spawnRemainingMs == 0L

        val bugs = if (shouldSpawn) {
            spawnRemainingMs = calculateSpawnInterval(activeDifficulty)
            movedBugs + createBug()
        } else {
            movedBugs
        }

        _state.value = _state.value.copy(
            remainingSeconds = ceil(remainingTimeMs / MILLIS_PER_SECOND.toFloat()).toInt(),
            bugs = bugs
        )
    }

    private fun finishRound(settings: GameSettings) {
        val state = _state.value
        val userName = activeUserName ?: return
        val finalScore = calculateRoundScore.execute(
            RoundScoreInput(
                points = state.score,
                penalties = state.penalties,
                difficulty = activeDifficulty,
                speed = settings.speed,
                maxCockroaches = settings.maxCockroaches,
                roundDurationSeconds = settings.roundDurationSeconds
            )
        )

        val result = GameResult(
            roundId = nextRoundId,
            userName = userName,
            rawScore = state.score,
            penalties = state.penalties,
            finalScore = finalScore,
            difficulty = activeDifficulty,
            roundDurationSeconds = settings.roundDurationSeconds,
            speed = settings.speed,
            maxCockroaches = settings.maxCockroaches
        )

        _state.value = state.copy(
            phase = GamePhase.FINISHED,
            remainingSeconds = 0,
            bugs = emptyList(),
            result = result
        )

        viewModelScope.launch {
            userRepository.updateBestScore(userName, finalScore)
        }
    }

    private fun moveBug(
        bug: GameBug,
        gameSpeed: Int,
        deltaSeconds: Float
    ): GameBug {
        val speed = BASE_SPEED * bug.type.speedMultiplier * gameSpeed
        var x = bug.x + bug.velocityX * speed * deltaSeconds
        var y = bug.y + bug.velocityY * speed * deltaSeconds
        var velocityX = bug.velocityX
        var velocityY = bug.velocityY

        if (x < BUG_HALF_SIZE || x > 1f - BUG_HALF_SIZE) {
            velocityX = -velocityX
            x = x.coerceIn(BUG_HALF_SIZE, 1f - BUG_HALF_SIZE)
        }

        if (y < BUG_HALF_SIZE || y > 1f - BUG_HALF_SIZE) {
            velocityY = -velocityY
            y = y.coerceIn(BUG_HALF_SIZE, 1f - BUG_HALF_SIZE)
        }

        return bug.copy(
            x = x,
            y = y,
            velocityX = velocityX,
            velocityY = velocityY
        )
    }

    private fun createBug(): GameBug {
        val type = BugType.entries.random(random)
        val angle = random.nextDouble(0.0, PI * 2)
        val directionX = cos(angle).toFloat()
        val directionY = sin(angle).toFloat()

        return GameBug(
            id = nextBugId++,
            type = type,
            x = randomCoordinate(),
            y = randomCoordinate(),
            velocityX = directionX,
            velocityY = directionY * 0.8f
        )
    }

    private fun randomCoordinate(): Float {
        val minimum = BUG_HALF_SIZE
        val maximum = 1f - BUG_HALF_SIZE

        return minimum + random.nextFloat() * (maximum - minimum)
    }

    private fun calculateSpawnInterval(difficulty: Int): Long {
        return when (difficulty) {
            MIN_DIFFICULTY -> SPAWN_INTERVAL_EASY_MS
            DEFAULT_DIFFICULTY -> SPAWN_INTERVAL_NORMAL_MS
            MAX_DIFFICULTY -> SPAWN_INTERVAL_HARD_MS
            else -> SPAWN_INTERVAL_NORMAL_MS
        }
    }

    private companion object {
        const val GAME_TICK_DELAY_MS = 16L
        const val MAX_DELTA_SECONDS = 0.05f
        const val BASE_SPEED = 0.14f
        const val BUG_HALF_SIZE = 0.07f
        const val BUG_HIT_HALF_SIZE = 0.09f
        const val MIN_DIFFICULTY = 1
        const val DEFAULT_DIFFICULTY = 2
        const val MAX_DIFFICULTY = 3
        const val SPAWN_INTERVAL_EASY_MS = 1400L
        const val SPAWN_INTERVAL_NORMAL_MS = 900L
        const val SPAWN_INTERVAL_HARD_MS = 500L
        const val MILLIS_PER_SECOND = 1000L
        const val NANOS_PER_SECOND = 1_000_000_000f
    }
}
