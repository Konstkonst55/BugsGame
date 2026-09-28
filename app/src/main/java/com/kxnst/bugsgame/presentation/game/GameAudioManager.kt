package com.kxnst.bugsgame.presentation.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.domain.game.GameRules

import kotlin.random.Random

class GameAudioManager(
    context: Context
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_SOUND_POOL_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val random = Random(System.currentTimeMillis())
    private val loadedSounds = mutableSetOf<Int>()
    private val movementStreams = mutableMapOf<Long, Int>()
    private val bugHitSoundId: Int
    private val penaltySoundId: Int
    private val bonusCollectedSoundId: Int
    private val bugScreamSoundId: Int
    private val bugMoveSoundId: Int

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds += sampleId
            }
        }

        bugHitSoundId = soundPool.load(context, R.raw.bug_click, 1)
        penaltySoundId = soundPool.load(context, R.raw.bug_penalty, 1)
        bonusCollectedSoundId = soundPool.load(context, R.raw.bonus_collect, 1)
        bugScreamSoundId = soundPool.load(context, R.raw.bug_scream, 1)
        bugMoveSoundId = soundPool.load(context, R.raw.bug_move, 1)
    }

    fun playBugHit() {
        playOneShot(bugHitSoundId)
    }

    fun playPenalty() {
        playOneShot(penaltySoundId)
    }

    fun playBonusCollected() {
        playOneShot(bonusCollectedSoundId)
    }

    fun playBonusActivation(bugCount: Int) {
        repeat(bugCount.coerceAtMost(GameRules.MAX_BUG_SOUND_SOURCES)) {
            playOneShot(bugScreamSoundId)
        }
    }

    fun updateBugMovementSounds(bugs: List<GameBug>) {
        if (bugs.isEmpty()) {
            stopBugMovementSounds()
            return
        }

        val selectedBugIds = bugs
            .asSequence()
            .take(GameRules.MAX_BUG_SOUND_SOURCES)
            .map(GameBug::id)
            .toSet()

        movementStreams.keys
            .filterNot { it in selectedBugIds }
            .toList()
            .forEach(::stopMovementSound)

        bugs.asSequence()
            .filter { it.id in selectedBugIds }
            .forEach { bug ->
                if (bug.id !in movementStreams && bugMoveSoundId in loadedSounds) {
                    movementStreams[bug.id] = soundPool.play(
                        bugMoveSoundId,
                        1f,
                        1f,
                        1,
                        -1,
                        randomPitch()
                    )
                }
            }
    }

    fun stopBugMovementSounds() {
        movementStreams.values.forEach(soundPool::stop)
        movementStreams.clear()
    }

    fun release() {
        stopBugMovementSounds()
        soundPool.release()
    }

    private fun stopMovementSound(bugId: Long) {
        movementStreams.remove(bugId)?.let(soundPool::stop)
    }

    private fun playOneShot(soundId: Int) {
        if (soundId in loadedSounds) {
            soundPool.play(
                soundId,
                1f,
                1f,
                2,
                0,
                randomPitch()
            )
        }
    }

    private fun randomPitch(): Float {
        return random.nextFloat().coerceIn(0f, 1f).let { value ->
            GameRules.MIN_SOUND_PITCH +
                value * (GameRules.MAX_SOUND_PITCH - GameRules.MIN_SOUND_PITCH)
        }
    }

    private companion object {
        const val EVENT_SOUND_STREAMS = 3
        const val MAX_SOUND_POOL_STREAMS =
            GameRules.MAX_BUG_SOUND_SOURCES * 2 + EVENT_SOUND_STREAMS
    }
}
