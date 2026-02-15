package com.twofasapp.feature.lock.ui.forgotpassword

internal data class ForgotPasswordUiState(
    val state: ForgotPasswordState = ForgotPasswordState.Default,
)

internal sealed interface ForgotPasswordState {
    data object Default : ForgotPasswordState
    data object Loading : ForgotPasswordState
    data object QrScan : ForgotPasswordState
    data class Error(val title: String, val msg: String) : ForgotPasswordState
}