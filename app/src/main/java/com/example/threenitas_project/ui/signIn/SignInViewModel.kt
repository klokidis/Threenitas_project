package com.example.threenitas_project.ui.signIn

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SignInState(
    val userId: String = "",
    val passwordText: String = "",
    val userIdInfo: Boolean = false,
    val passwordInfo: Boolean = false,
    val isValidText: Boolean = true,
    val isValidPassword: Boolean = true,
    val showWrongSignIn: Boolean = false,
)

class SignInViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(SignInState())
    val uiState: StateFlow<SignInState> = _uiState.asStateFlow()


    fun changeNameText(newValue: String) {
        _uiState.update { currentState ->
            currentState.copy(
                userId = newValue,
                isValidText = checkUserId(newValue)
            )
        }
    }

    fun changePasswordText(newValue: String) {
        _uiState.update { currentState ->
            currentState.copy(
                passwordText = newValue,
                isValidPassword = checkPassword(newValue)
            )
        }
    }

    fun changeUserIdInfo(newValue: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                userIdInfo = newValue
            )
        }
    }

    fun changePasswordInfo(newValue: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                passwordInfo = newValue
            )
        }
    }

    fun checkSignIn() {
        _uiState.update { currentState ->
            currentState.copy(
                showWrongSignIn = !(checkUserId(uiState.value.userId) && checkPassword(uiState.value.passwordText))
            )
        }
    }

    fun hideShowWrongSignIn() {
        _uiState.update { currentState ->
            currentState.copy(
                showWrongSignIn = false
            )
        }
    }

    private fun checkUserId(input: String): Boolean {
        // check if the text starts with 2 capitals and then 4 digits
        val regex = "^[A-Z]{2}\\d{4}".toRegex()
        return regex.matches(input)
    }

    private fun checkPassword(password: String): Boolean {
        // checks 8 characters length, 2 letters in Upper Case , 1 Special Character, 2 numerals (0-9), 3 letters in Lower Case.
        val regex =
            "^(?=(.*[A-Z]){2})(?=(.*[a-z]){3})(?=(.*\\d){2})(?=(.*[!@#\$%^&*(),.?\":{}|<>])).{8,}$".toRegex()
        return regex.matches(password)
    }
}