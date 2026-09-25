package com.kxnst.bugsgame.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.kxnst.bugsgame.data.user.UserRepository
import com.kxnst.bugsgame.domain.user.UserProfile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserSessionViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _usersState = MutableStateFlow<UserListState>(UserListState.Loading)
    val usersState: StateFlow<UserListState> = _usersState.asStateFlow()

    init {
        observeUsers()
    }

    fun setCurrentUser(user: UserProfile) {
        _currentUser.value = user
    }

    fun selectUser(name: String) {
        viewModelScope.launch {
            userRepository.getUser(name)?.let { user ->
                _currentUser.value = user
            }
        }
    }

    private fun observeUsers() {
        viewModelScope.launch {
            runCatching {
                userRepository.observeUsers().collect { users ->
                    _usersState.value = if (users.isEmpty()) {
                        UserListState.Empty
                    } else {
                        UserListState.Content(users)
                    }

                    val currentUserName = _currentUser.value?.name

                    if (currentUserName != null) {
                        _currentUser.value = users.firstOrNull { it.name == currentUserName }
                    }
                }
            }.onFailure { error ->
                _usersState.value = UserListState.Error(error.message.orEmpty())
            }
        }
    }
}
