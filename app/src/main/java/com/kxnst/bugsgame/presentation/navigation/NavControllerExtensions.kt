package com.kxnst.bugsgame.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.navOptions

import com.kxnst.bugsgame.R

fun NavController.navigateToHome() {
    if (popBackStack(R.id.homeFragment, false)) {
        return
    }

    navigate(
        R.id.homeFragment,
        null,
        navOptions {
            popUpTo(R.id.registerFragment) {
                inclusive = true
            }
            launchSingleTop = true
        }
    )
}
