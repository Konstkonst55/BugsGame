package com.kxnst.bugsgame.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.kxnst.bugsgame.data.settings.GameSettings
import com.kxnst.bugsgame.data.settings.GameSettingsRepository
import com.kxnst.bugsgame.data.user.UserRepository
import com.kxnst.bugsgame.domain.game.CalculateRoundScoreUseCase
import com.kxnst.bugsgame.domain.game.GameRules
import com.kxnst.bugsgame.domain.game.RoundScoreInput
import com.kxnst.bugsgame.domain.user.UserProfile

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _soundEvents = MutableSharedFlow<GameSoundEvent>(extraBufferCapacity = 8)
    val soundEvents: SharedFlow<GameSoundEvent> = _soundEvents.asSharedFlow()

    private val random = Random(System.currentTimeMillis())
    private var gameJob: Job? = null
    private var activeSettings: GameSettings? = null
    private var activeUserName: String? = null
    private var remainingTimeMs = 0L
    private var spawnRemainingMs = 0L
    private var bonusSpawnRemainingMs = 0L
    private var bonusRemainingMs = 0L
    private var nextBugId = 0L
    private var nextRoundId = 0L
    private var activeDifficulty = GameRules.DEFAULT_DIFFICULTY
    private var tiltX = 0f
    private var tiltY = 0f

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
        activeDifficulty = user.difficulty.coerceIn(
            GameRules.MIN_DIFFICULTY,
            GameRules.MAX_DIFFICULTY
        )
        remainingTimeMs = settings.roundDurationSeconds * GameRules.MILLIS_PER_SECOND
        spawnRemainingMs = 0L
        bonusSpawnRemainingMs = settings.bonusIntervalSeconds * GameRules.MILLIS_PER_SECOND
        bonusRemainingMs = 0L
        tiltX = 0f
        tiltY = 0f

        nextRoundId += 1
        _state.value = GameState(
            phase = GamePhase.RUNNING,
            score = 0,
            penalties = 0,
            remainingSeconds = settings.roundDurationSeconds,
            bugs = emptyList(),
            bonus = null,
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

    fun updateTilt(x: Float, y: Float) {
        tiltX = x.coerceIn(-1f, 1f)
        tiltY = y.coerceIn(-1f, 1f)
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
        val bonus = state.bonus

        if (bonus != null && isInside(x, y, bonus.x, bonus.y, BONUS_HIT_HALF_SIZE)) {
            activateBonus(state)
            return
        }

        val hitIndex = state.bugs.indexOfLast { bug ->
            isInside(x, y, bug.x, bug.y, BUG_HIT_HALF_SIZE)
        }

        if (hitIndex == -1) {
            _state.value = state.copy(penalties = state.penalties + 1)
            _soundEvents.tryEmit(GameSoundEvent.Penalty)
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
        _soundEvents.tryEmit(GameSoundEvent.BugHit)
    }

    private fun activateBonus(state: GameState) {
        bonusRemainingMs = GameRules.BONUS_DURATION_SECONDS * GameRules.MILLIS_PER_SECOND
        _state.value = state.copy(bonus = null)
        _soundEvents.tryEmit(GameSoundEvent.BonusCollected)
        _soundEvents.tryEmit(GameSoundEvent.BonusActivated(bugCount = state.bugs.size))
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
        val deltaMs = (deltaSeconds * GameRules.MILLIS_PER_SECOND).toLong()

        remainingTimeMs = (remainingTimeMs - deltaMs).coerceAtLeast(0L)
        spawnRemainingMs = (spawnRemainingMs - deltaMs).coerceAtLeast(0L)
        bonusSpawnRemainingMs = (bonusSpawnRemainingMs - deltaMs).coerceAtLeast(0L)

        if (remainingTimeMs == 0L) {
            finishRound(settings)
            return
        }

        val bonusModeWasActive = bonusRemainingMs > 0L

        if (bonusModeWasActive) {
            bonusRemainingMs = (bonusRemainingMs - deltaMs).coerceAtLeast(0L)
        }

        val currentBugs = _state.value.bugs
        val movedBugs = if (bonusModeWasActive) {
            currentBugs.map { bug -> moveBugWithTilt(bug, deltaSeconds) }
        } else {
            currentBugs.map { bug -> moveBugNormally(bug, settings.speed, deltaSeconds) }
        }

        val bugsAfterBonus = if (bonusModeWasActive && bonusRemainingMs == 0L) {
            scatterBugs(movedBugs)
        } else {
            movedBugs
        }

        val shouldSpawnBug =
            bugsAfterBonus.size < settings.maxCockroaches &&
                spawnRemainingMs == 0L

        val bugs = if (shouldSpawnBug) {
            spawnRemainingMs = calculateSpawnInterval(activeDifficulty)
            bugsAfterBonus + createBug()
        } else {
            bugsAfterBonus
        }

        val currentState = _state.value
        val bonus = currentState.bonus ?: spawnBonusIfNeeded(settings)

        _state.value = currentState.copy(
            remainingSeconds = ceil(
                remainingTimeMs / GameRules.MILLIS_PER_SECOND.toFloat()
            ).toInt(),
            bugs = bugs,
            bonus = bonus
        )
    }

    private fun spawnBonusIfNeeded(settings: GameSettings): GameBonus? {
        if (bonusSpawnRemainingMs > 0L || bonusRemainingMs > 0L) {
            return null
        }

        bonusSpawnRemainingMs = settings.bonusIntervalSeconds * GameRules.MILLIS_PER_SECOND

        return GameBonus(
            x = randomCoordinate(BONUS_HALF_SIZE),
            y = randomCoordinate(BONUS_HALF_SIZE)
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
            bonus = null,
            result = result
        )

        viewModelScope.launch {
            userRepository.updateBestScore(userName, finalScore)
        }
    }

    private fun moveBugNormally(
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

    private fun moveBugWithTilt(
        bug: GameBug,
        deltaSeconds: Float
    ): GameBug {
        var velocityX = bug.velocityX + tiltX * BONUS_ACCELERATION * deltaSeconds
        var velocityY = bug.velocityY + tiltY * BONUS_ACCELERATION * deltaSeconds
        val damping = exp(-BONUS_FRICTION * deltaSeconds)

        velocityX *= damping
        velocityY *= damping

        val velocityMagnitude = sqrt(velocityX * velocityX + velocityY * velocityY)
        if (velocityMagnitude > BONUS_MAX_VELOCITY) {
            val scale = BONUS_MAX_VELOCITY / velocityMagnitude
            velocityX *= scale
            velocityY *= scale
        }

        var x = bug.x + velocityX * deltaSeconds
        var y = bug.y + velocityY * deltaSeconds

        if (x < BUG_HALF_SIZE || x > 1f - BUG_HALF_SIZE) {
            velocityX = -velocityX * BONUS_BOUNCE_FACTOR
            x = x.coerceIn(BUG_HALF_SIZE, 1f - BUG_HALF_SIZE)
        }

        if (y < BUG_HALF_SIZE || y > 1f - BUG_HALF_SIZE) {
            velocityY = -velocityY * BONUS_BOUNCE_FACTOR
            y = y.coerceIn(BUG_HALF_SIZE, 1f - BUG_HALF_SIZE)
        }

        return bug.copy(
            x = x,
            y = y,
            velocityX = velocityX,
            velocityY = velocityY
        )
    }

    private fun scatterBugs(bugs: List<GameBug>): List<GameBug> {
        if (bugs.isEmpty()) {
            return bugs
        }

        val angleStep = (PI * 2.0 / bugs.size).toFloat()
        val startAngle = random.nextFloat() * angleStep

        return bugs.mapIndexed { index, bug ->
            val angle = startAngle + angleStep * index
            val velocity = random.nextFloat().coerceIn(
                BONUS_SCATTER_MIN_SPEED,
                BONUS_SCATTER_MAX_SPEED
            )

            bug.copy(
                velocityX = cos(angle) * velocity,
                velocityY = sin(angle) * velocity
            )
        }
    }

    private fun createBug(): GameBug {
        val type = BugType.entries.random(random)
        val angle = random.nextDouble(0.0, PI * 2)
        val directionX = cos(angle).toFloat()
        val directionY = sin(angle).toFloat()

        return GameBug(
            id = nextBugId++,
            type = type,
            x = randomCoordinate(BUG_HALF_SIZE),
            y = randomCoordinate(BUG_HALF_SIZE),
            velocityX = directionX,
            velocityY = directionY * 0.8f
        )
    }

    private fun randomCoordinate(halfSize: Float): Float {
        return halfSize + random.nextFloat() * (1f - 2f * halfSize)
    }

    private fun calculateSpawnInterval(difficulty: Int): Long {
        return when (difficulty) {
            GameRules.MIN_DIFFICULTY -> GameRules.SPAWN_INTERVAL_EASY_MS
            GameRules.DEFAULT_DIFFICULTY -> GameRules.SPAWN_INTERVAL_NORMAL_MS
            GameRules.MAX_DIFFICULTY -> GameRules.SPAWN_INTERVAL_HARD_MS
            else -> GameRules.SPAWN_INTERVAL_NORMAL_MS
        }
    }

    private fun isInside(
        x: Float,
        y: Float,
        centerX: Float,
        centerY: Float,
        halfSize: Float
    ): Boolean {
        return abs(centerX - x) <= halfSize && abs(centerY - y) <= halfSize
    }

    private companion object {
        const val GAME_TICK_DELAY_MS = 16L
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val MAX_DELTA_SECONDS = 0.05f
        const val BASE_SPEED = 0.14f
        const val BUG_HALF_SIZE = 0.07f
        const val BUG_HIT_HALF_SIZE = 0.09f
        const val BONUS_HALF_SIZE = 0.08f
        const val BONUS_HIT_HALF_SIZE = 0.11f
        const val BONUS_ACCELERATION = 1.8f
        const val BONUS_FRICTION = 3.0f
        const val BONUS_MAX_VELOCITY = 1.25f
        const val BONUS_BOUNCE_FACTOR = 0.65f
        const val BONUS_SCATTER_MIN_SPEED = 0.35f
        const val BONUS_SCATTER_MAX_SPEED = 0.8f
    }
}
