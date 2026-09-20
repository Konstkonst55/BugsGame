package com.kxnst.bugsgame.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.kxnst.bugsgame.data.settings.GameSettingsRepository

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val settingsRepository: GameSettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val random = Random(System.currentTimeMillis())
    private var gameJob: Job? = null
    private var nextSpawnAt = 0L
    private var nextBugId = 0L

    fun startGame() {
        if (gameJob?.isActive == true) {
            return
        }

        gameJob = viewModelScope.launch {
            runGameLoop()
        }
    }

    fun pauseGame() {
        gameJob?.cancel()
        gameJob = null
    }

    fun handleTap(x: Float, y: Float) {
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

    override fun onCleared() {
        pauseGame()
        super.onCleared()
    }

    private suspend fun runGameLoop() {
        var lastTickAt = System.nanoTime()

        while (currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            val currentTickAt = System.nanoTime()
            val deltaSeconds = ((currentTickAt - lastTickAt) / 1_000_000_000f)
                .coerceIn(0f, MAX_DELTA_SECONDS)

            lastTickAt = currentTickAt
            updateGame(now, deltaSeconds)

            delay(GAME_TICK_DELAY_MS)
        }
    }

    private fun updateGame(now: Long, deltaSeconds: Float) {
        val settings = settingsRepository.settings.value
        val movedBugs = _state.value.bugs.map { bug ->
            moveBug(bug, settings.speed, deltaSeconds)
        }

        val shouldSpawn =
            movedBugs.size < settings.maxCockroaches && now >= nextSpawnAt

        val bugs = if (shouldSpawn) {
            nextSpawnAt = now + calculateSpawnInterval(settings.speed)
            movedBugs + createBug(settings.speed)
        } else {
            movedBugs
        }

        _state.value = _state.value.copy(bugs = bugs)
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

    private fun createBug(gameSpeed: Int): GameBug {
        val type = BugType.entries.random(random)
        val angle = random.nextDouble(0.0, PI * 2)
        val directionX = cos(angle).toFloat()
        val directionY = sin(angle).toFloat()
        val minimumCoordinate = BUG_HALF_SIZE
        val maximumCoordinate = 1f - BUG_HALF_SIZE

        return GameBug(
            id = nextBugId++,
            type = type,
            x = randomCoordinate(minimumCoordinate, maximumCoordinate),
            y = randomCoordinate(minimumCoordinate, maximumCoordinate),
            velocityX = directionX,
            velocityY = directionY * (0.8f + gameSpeed * 0.02f)
        )
    }

    private fun randomCoordinate(minimum: Float, maximum: Float): Float {
        return minimum + random.nextFloat() * (maximum - minimum)
    }

    private fun calculateSpawnInterval(gameSpeed: Int): Long {
        return (SPAWN_INTERVAL_BASE_MS - gameSpeed * SPAWN_INTERVAL_SPEED_FACTOR_MS)
            .coerceAtLeast(SPAWN_INTERVAL_MIN_MS)
    }

    private companion object {
        const val GAME_TICK_DELAY_MS = 16L
        const val MAX_DELTA_SECONDS = 0.05f
        const val BASE_SPEED = 0.14f
        const val BUG_HALF_SIZE = 0.07f
        const val BUG_HIT_HALF_SIZE = 0.09f
        const val SPAWN_INTERVAL_BASE_MS = 1200L
        const val SPAWN_INTERVAL_SPEED_FACTOR_MS = 70L
        const val SPAWN_INTERVAL_MIN_MS = 450L
    }
}
