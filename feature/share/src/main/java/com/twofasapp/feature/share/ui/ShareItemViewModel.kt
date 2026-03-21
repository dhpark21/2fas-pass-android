/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.ui

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.share.ShareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ShareItemViewModel(
    private val item: Item,
    private val itemEncryptionMapper: ItemEncryptionMapper,
    private val vaultCryptoScope: VaultCryptoScope,
    private val shareRepository: ShareRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(ShareItemUiState(item = item))

    fun setExpirationTime(expirationTime: ExpirationTime) {
        uiState.update { it.copy(expirationTime = expirationTime) }
    }

    fun toggleOneTimeAccess() {
        uiState.update { it.copy(oneTimeAccess = it.oneTimeAccess.not()) }
    }

    fun setPassword(password: String?) {
        uiState.update { it.copy(password = password) }
    }

    fun generateLink() {
        uiState.update { it.copy(screenState = ShareItemScreenState.Loading) }

        launchScoped {
            runSafely {
                val decryptedContent = vaultCryptoScope.withVaultCipher(item.vaultId) {
                    itemEncryptionMapper.decryptSecretFields(this, item.securityType, item.content)
                }

                shareRepository.createShareLink(
                    item = item.copy(content = decryptedContent),
                    expirationTimeSeconds = uiState.value.expirationTime.seconds,
                    oneTimeAccess = uiState.value.oneTimeAccess,
                    password = uiState.value.password,
                )
            }
                .onSuccess { shareLink ->
                    uiState.update {
                        it.copy(
                            screenState = ShareItemScreenState.Success,
                            link = shareLink.url,
                        )
                    }
                }
                .onFailure {
                    uiState.update { it.copy(screenState = ShareItemScreenState.Form) }
                }
        }
    }
}