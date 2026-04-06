/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.ui.sharelinkhandler

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.data.share.ShareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class ShareLinkHandlerViewModel(
    savedStateHandle: SavedStateHandle,
    private val shareRepository: ShareRepository,
) : ViewModel() {

    private val route: Screen.ShareLinkHandler = savedStateHandle.toRoute<Screen.ShareLinkHandler>()

    val uiState = MutableStateFlow(ShareLinkHandlerUiState())

    init {
        when (route.version) {
            "v1p" -> uiState.update { it.copy(shouldShowPasswordDialog = true) }
            "v1k" -> decrypt(password = null)
            else -> uiState.update { it.copy(shouldClose = true) }
        }
    }

    fun submitPassword(password: String) {
        decrypt(password = password)
    }

    fun consumeDecrypted() {
        uiState.update { it.copy(decryptedShareId = null, decryptedItemContentTypeKey = null) }
    }

    private fun decrypt(password: String?) {
        uiState.update { it.copy(loading = true, error = false) }

        launchScoped {
            runSafely {
                shareRepository.decryptShareLink(
                    shareId = route.shareId,
                    version = route.version,
                    nonce = route.nonce,
                    key = route.key,
                    password = password,
                )
            }
                .onSuccess { decryptedItem ->
                    Timber.d("Decrypted share link: $decryptedItem")
                    shareRepository.cacheDecryptedShareItem(route.shareId, decryptedItem)
                    uiState.update {
                        it.copy(
                            loading = false,
                            decryptedShareId = route.shareId,
                            decryptedItemContentTypeKey = decryptedItem.contentType.key,
                        )
                    }
                }
                .onFailure { e ->
                    uiState.update {
                        it.copy(
                            loading = false,
                            error = true,
                            shouldShowPasswordDialog = route.version == "v1p",
                        )
                    }
                }
        }
    }
}