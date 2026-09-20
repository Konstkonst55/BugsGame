package com.kxnst.bugsgame.di

import com.kxnst.bugsgame.data.settings.GameSettingsRepository
import com.kxnst.bugsgame.data.settings.SharedPreferencesGameSettingsRepository
import com.kxnst.bugsgame.data.zodiac.AndroidResourceZodiacRepository
import com.kxnst.bugsgame.data.zodiac.ZodiacRepository
import com.kxnst.bugsgame.domain.zodiac.ZodiacCalculator
import com.kxnst.bugsgame.presentation.game.GameViewModel
import com.kxnst.bugsgame.presentation.register.PlayerViewModel
import com.kxnst.bugsgame.presentation.settings.GameSettingsViewModel

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<ZodiacRepository> { AndroidResourceZodiacRepository(androidContext().resources) }
    single { ZodiacCalculator() }
    single<GameSettingsRepository> { SharedPreferencesGameSettingsRepository(androidContext()) }

    viewModel { PlayerViewModel(get(), get()) }
    viewModel { GameSettingsViewModel(get()) }
    viewModel { GameViewModel(get()) }
}
