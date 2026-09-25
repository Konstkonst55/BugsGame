package com.kxnst.bugsgame.di

import androidx.room.Room

import com.kxnst.bugsgame.data.database.BugsGameDatabase
import com.kxnst.bugsgame.data.settings.GameSettingsRepository
import com.kxnst.bugsgame.data.settings.SharedPreferencesGameSettingsRepository
import com.kxnst.bugsgame.data.user.RoomUserRepository
import com.kxnst.bugsgame.data.user.UserRepository
import com.kxnst.bugsgame.data.zodiac.AndroidResourceZodiacRepository
import com.kxnst.bugsgame.data.zodiac.ZodiacRepository
import com.kxnst.bugsgame.domain.game.CalculateRoundScoreUseCase
import com.kxnst.bugsgame.domain.zodiac.ZodiacCalculator
import com.kxnst.bugsgame.presentation.game.GameViewModel
import com.kxnst.bugsgame.presentation.register.PlayerViewModel
import com.kxnst.bugsgame.presentation.settings.GameSettingsViewModel
import com.kxnst.bugsgame.presentation.user.UserSessionViewModel

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            BugsGameDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    single { get<BugsGameDatabase>().userDao() }
    single<UserRepository> { RoomUserRepository(get()) }
    single<GameSettingsRepository> { SharedPreferencesGameSettingsRepository(androidContext()) }
    single<ZodiacRepository> { AndroidResourceZodiacRepository(androidContext().resources) }
    single { ZodiacCalculator() }
    single { CalculateRoundScoreUseCase() }

    viewModel { PlayerViewModel(get(), get(), get()) }
    viewModel { UserSessionViewModel(get()) }
    viewModel { GameSettingsViewModel(get()) }
    viewModel { GameViewModel(get(), get(), get()) }
}

private const val DATABASE_NAME = "bugsgame.db"
