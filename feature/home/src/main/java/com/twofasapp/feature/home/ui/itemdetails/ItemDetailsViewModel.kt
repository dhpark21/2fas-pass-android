/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.cardNumberGrouping
import com.twofasapp.core.common.domain.items.defaultCardNumberGrouping
import com.twofasapp.core.common.domain.items.formatWithGrouping
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: Dispatchers,
    private val itemsRepository: ItemsRepository,
    private val tagsRepository: TagsRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {

    private val itemId: String = savedStateHandle.toRoute<Screen.ItemDetails>().itemId
    private val vaultId: String = savedStateHandle.toRoute<Screen.ItemDetails>().vaultId

    val uiState = MutableStateFlow(ItemDetailsUiState())

    init {
        launchScoped(dispatchers.io) {
            vaultCryptoScope.withVaultCipher(vaultId) {
                val vaultCipher = this

                combine(
                    itemsRepository.observeItem(vaultId, itemId)
                        .map { itemEncrypted ->
                            itemEncryptionMapper.decryptItem(
                                itemEncrypted = itemEncrypted,
                                vaultCipher = vaultCipher,
                                decryptSecretFields = false,
                            )
                        }
                        .filterNotNull(),
                    tagsRepository.observeTags(vaultId),
                ) { item, tags ->
                    item to tags
                }.collect { (item, tags) ->
                    uiState.update {
                        it.copy(
                            item = item,
                            tags = tags.toImmutableList(),
                            decryptedFields = if (it.item.id != item.id) emptyMap() else it.decryptedFields,
                        )
                    }

                    // Auto-decrypt SecureNote for Tier3
                    if (item.securityType == SecurityType.Tier3 && item.content is ItemContent.SecureNote) {
                        val content = item.content as ItemContent.SecureNote
                        content.text?.let { secretField ->
                            decryptSecretField(SecretFieldType.SecureNoteText, secretField)
                        }
                    }
                }
            }
        }
    }

    fun toggleSecretField(fieldType: SecretFieldType, secretField: SecretField?) {
        if (secretField == null) return

        val currentValue = uiState.value.decryptedFields[fieldType]
        if (currentValue != null) {
            // Hide the field
            uiState.update {
                it.copy(decryptedFields = it.decryptedFields - fieldType)
            }
        } else {
            // Decrypt and show the field
            decryptSecretField(fieldType, secretField)
        }
    }

    fun copySecretFieldToClipboard(secretField: SecretField?, onDecrypted: (String) -> Unit) {
        if (secretField == null) return

        launchScoped(dispatchers.io) {
            vaultCryptoScope.withVaultCipher(uiState.value.item.vaultId) {
                itemEncryptionMapper.decryptSecretField(
                    secretField = secretField,
                    securityType = uiState.value.item.securityType,
                    vaultCipher = this,
                )?.let { decrypted ->
                    launchScoped(dispatchers.main) {
                        onDecrypted(decrypted)
                    }
                }
            }
        }
    }

    fun clearDecryptedFields() {
        uiState.update {
            it.copy(decryptedFields = emptyMap())
        }
    }

    private fun decryptSecretField(fieldType: SecretFieldType, secretField: SecretField) {
        launchScoped(dispatchers.io) {
            vaultCryptoScope.withVaultCipher(uiState.value.item.vaultId) {
                itemEncryptionMapper.decryptSecretField(
                    secretField = secretField,
                    securityType = uiState.value.item.securityType,
                    vaultCipher = this,
                )?.let { decrypted ->
                    val formattedValue = when (fieldType) {
                        SecretFieldType.PaymentCardNumber -> {
                            val content = uiState.value.item.content as? ItemContent.PaymentCard
                            decrypted.removeWhitespace()
                                .formatWithGrouping(
                                    content?.cardIssuer?.cardNumberGrouping()
                                        ?: defaultCardNumberGrouping,
                                )
                        }

                        SecretFieldType.PaymentCardExpiration,
                        SecretFieldType.PaymentCardSecurityCode,
                        -> {
                            decrypted.removeWhitespace()
                        }

                        else -> decrypted
                    }

                    uiState.update {
                        it.copy(decryptedFields = it.decryptedFields + (fieldType to formattedValue))
                    }
                }
            }
        }
    }
}