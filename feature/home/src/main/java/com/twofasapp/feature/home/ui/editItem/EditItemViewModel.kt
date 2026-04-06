/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.editItem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.domain.normalizeBeforeSaving
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.share.ShareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

internal class EditItemViewModel(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: Dispatchers,
    private val itemsRepository: ItemsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
    private val shareRepository: ShareRepository,
    private val vaultsRepository: VaultsRepository,
) : ViewModel() {

    private val route: Screen.EditItem = savedStateHandle.toRoute<Screen.EditItem>()
    private val id: String = route.itemId
    private val vaultId: String = route.vaultId
    private val itemContentType: ItemContentType = ItemContentType.fromKey(route.itemContentTypeKey)

    private val isShareLink = route.shareId != null
    private val isNewItem = id.isBlank() && !isShareLink
    val uiState = MutableStateFlow(EditItemUiState(isShareLink = isShareLink))

    init {
        when {
            isShareLink -> {
                val sharedItem = shareRepository.getDecryptedShareItem(route.shareId!!)
                shareRepository.removeDecryptedShareItem(route.shareId!!)

                if (sharedItem != null) {
                    uiState.update { state ->
                        state.copy(
                            initialItem = sharedItem,
                            item = sharedItem,
                        )
                    }
                }
            }

            isNewItem -> {
                launchScoped {
                    val initialItem = Item.create(
                        contentType = itemContentType,
                        content = when (itemContentType) {
                            is ItemContentType.Login -> ItemContent.Login.Empty
                            is ItemContentType.SecureNote -> ItemContent.SecureNote.Empty
                            is ItemContentType.PaymentCard -> ItemContent.PaymentCard.Empty
                            is ItemContentType.Wifi -> ItemContent.Wifi.Empty
                            is ItemContentType.Unknown -> ItemContent.Unknown("")
                        },
                    )

                    uiState.update { state ->
                        state.copy(
                            initialItem = initialItem,
                            item = initialItem,
                        )
                    }
                }
            }

            else -> {
                launchScoped(dispatchers.io) {
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        val item = itemsRepository.getItem(id)
                        val initialItem = item
                            .let {
                                itemEncryptionMapper.decryptItem(
                                    itemEncrypted = it,
                                    vaultCipher = this,
                                    decryptSecretFields = true,
                                )
                            } ?: return@withVaultCipher

                        uiState.update { state ->
                            state.copy(
                                initialItem = initialItem,
                                item = initialItem,
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateItem(item: Item) {
        uiState.update { it.copy(item = item) }
    }

    fun updateIsValid(isValid: Boolean) {
        uiState.update { it.copy(isValid = isValid) }
    }

    fun updateHasUnsavedChanges(hasUnsavedChanges: Boolean) {
        uiState.update { it.copy(hasUnsavedChanges = hasUnsavedChanges) }
    }

    fun save(onComplete: () -> Unit) {
        launchScoped {
            val resolvedVaultId = vaultId.ifBlank { vaultsRepository.getVault().id }
            val item = withContext(dispatchers.io) {
                itemEncryptionMapper.encryptItem(
                    item = uiState.value.item
                        .copy(vaultId = resolvedVaultId)
                        .normalizeBeforeSaving(),
                    vaultCipher = vaultCryptoScope.getVaultCipher(resolvedVaultId),
                )
            }

            itemsRepository.saveItem(item)
        }.invokeOnCompletion { onComplete() }
    }
}