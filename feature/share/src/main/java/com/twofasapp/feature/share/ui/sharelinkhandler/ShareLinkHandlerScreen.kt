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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.progress.CircularProgress
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.share.ui.SharePasswordDialog
import com.twofasapp.feature.share.ui.SharePasswordDialogMode
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
        if (uiState.loading && uiState.shouldShowPasswordDialog.not()) {
            CircularProgress()
        }
    }

    if (uiState.shouldShowPasswordDialog) {
        SharePasswordDialog(
            mode = SharePasswordDialogMode.EnterPassword,
            loading = uiState.loading,
            errorText = if (uiState.error) strings.shareLinkImportIncorrectPassword else null,
            dismissOnSubmit = false,
            onDismissRequest = onClose,
            onSubmit = { password -> onPasswordSubmit(password) },
        )
    }

    if (uiState.error && uiState.shouldShowPasswordDialog.not() && uiState.loading.not()) {
        InfoDialog(
            onDismissRequest = onClose,
            icon = MdtIcons.Warning,
            title = strings.shareLinkImportErrorTitle,
            body = strings.shareLinkImportErrorDescription,
            onPositive = onClose,
        )
    }
}