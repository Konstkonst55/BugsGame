package com.kxnst.bugsgame.di

import com.kxnst.bugsgame.data.zodiac.AndroidResourceZodiacRepository
import com.kxnst.bugsgame.data.zodiac.ZodiacRepository
import com.kxnst.bugsgame.domain.zodiac.ZodiacCalculator
import com.kxnst.bugsgame.presentation.register.PlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val appModule = module {
    single<ZodiacRepository> { AndroidResourceZodiacRepository(androidContext().resources) }
    single { ZodiacCalculator() }
    viewModel { PlayerViewModel(get(), get()) }
}
