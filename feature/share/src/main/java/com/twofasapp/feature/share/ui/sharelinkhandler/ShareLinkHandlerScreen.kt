/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.ui.sharelinkhandler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.dialogScreenMargin
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.dialog.InputDialog
import com.twofasapp.core.design.foundation.dialog.InputValidation
import com.twofasapp.core.design.foundation.progress.CircularProgress
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ShareLinkHandlerScreen(
    viewModel: ShareLinkHandlerViewModel = koinViewModel(),
    onDecrypted: (shareId: String, itemContentTypeKey: String) -> Unit,
    close: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.decryptedShareId) {
        val shareId = uiState.decryptedShareId
        val itemContentTypeKey = uiState.decryptedItemContentTypeKey
        if (shareId != null && itemContentTypeKey != null) {
            viewModel.consumeDecrypted()
            onDecrypted(shareId, itemContentTypeKey)
        }
    }

    LaunchedEffect(uiState.shouldClose) {
        if (uiState.shouldClose) {
            close()
        }
    }

    Content(
        uiState = uiState,
        onPasswordSubmit = viewModel::submitPassword,
        onClose = close,
    )
}

@Composable
private fun Content(
    uiState: ShareLinkHandlerUiState,
    onPasswordSubmit: (String) -> Unit = {},
    onClose: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (uiState.loading) {
            CircularProgress()
        }
    }

    if (uiState.shouldShowPasswordDialog) {
        InputDialog(
            onDismissRequest = {},
            modifier = Modifier.padding(horizontal = dialogScreenMargin),
            title = "Enter password",
            body = "This share link is password protected.",
            positive = strings.commonContinue,
            negative = strings.commonCancel,
            icon = MdtIcons.Lock,
            label = strings.masterPasswordLabel,
            validate = { input ->
                if (input.length < 8) {
                    InputValidation.Invalid("Minimum 8 characters")
                } else {
                    InputValidation.Valid
                }
            },
            onPositive = { password -> onPasswordSubmit(password.trim()) },
            onNegative = onClose,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            isSecret = true,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        )
    }

    if (uiState.error && uiState.shouldShowPasswordDialog.not() && uiState.loading.not()) {
        InfoDialog(
            onDismissRequest = onClose,
            icon = MdtIcons.Warning,
            title = "Failed to open share link",
            body = "We couldn't decrypt the share link. It may have expired or the password is incorrect.",
            onPositive = onClose,
        )
    }
}