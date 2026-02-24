/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.cardNumberGrouping
import com.twofasapp.core.common.domain.items.defaultCardNumberGrouping
import com.twofasapp.core.common.domain.items.formatWithGrouping
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ItemDetailsModalViewModel(
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemEncryptionMapper: ItemEncryptionMapper,
    private val dispatchers: Dispatchers,
) : ViewModel() {

    val uiState = MutableStateFlow(ItemDetailsModalUiState())

    fun initialize(item: Item, tags: ImmutableList<Tag>) {
        uiState.update {
            it.copy(item = item, tags = tags)
        }

        // Auto-decrypt SecureNote for Tier3
        if (item.securityType == SecurityType.Tier3 && item.content is ItemContent.SecureNote) {
            val content = item.content as ItemContent.SecureNote
            content.text?.let { secretField ->
                decryptSecretField(SecretFieldType.SecureNoteText, secretField)
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
                    onDecrypted(decrypted)
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
                                        ?: defaultCardNumberGrouping
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