package com.kxnst.bugsgame.presentation.user

import com.kxnst.bugsgame.domain.user.UserProfile

sealed interface UserListState {
    data object Loading : UserListState
    data class Content(val users: List<UserProfile>) : UserListState
    data object Empty : UserListState
    data class Error(val message: String) : UserListState
}
