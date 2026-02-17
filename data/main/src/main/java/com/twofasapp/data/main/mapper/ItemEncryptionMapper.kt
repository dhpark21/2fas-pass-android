/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.domain.VaultKeysExpiredException

class ItemEncryptionMapper(
    private val mapperDelegate: ItemEncryptionMapperDelegate
) {
    fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean = false,
    ): Item? {
        return try {
            val contentEntityJson = when (itemEncrypted.securityType) {
                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(itemEncrypted.content)
                SecurityType.Tier2 -> vaultCipher.decryptWithTrustedKey(itemEncrypted.content)
                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(itemEncrypted.content)
            }

            val content = mapperDelegate.decryptItem(
                itemEncrypted,
                vaultCipher,
                decryptSecretFields,
                contentEntityJson
            )

            itemEncrypted.asDecrypted(content = content)
        } catch (_: VaultKeysExpiredException) {
            null
        }
    }

    fun encryptItem(
        item: Item,
        vaultCipher: VaultCipher,
    ): ItemEncrypted {
        val contentEntityJson = mapperDelegate.encryptItem(item, item.content, vaultCipher)

        val contentEntityJsonEncrypted = when (item.securityType) {
            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(contentEntityJson)
            SecurityType.Tier2 -> vaultCipher.encryptWithTrustedKey(contentEntityJson)
            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(contentEntityJson)
        }

        return item.asEncrypted(content = contentEntityJsonEncrypted)
    }

    fun encryptItems(
        items: List<Item>,
        vaultCipher: VaultCipher,
    ): List<ItemEncrypted> {
        return items.map { encryptItem(item = it, vaultCipher = vaultCipher) }
    }

    fun decryptSecretField(
        secretField: SecretField?,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
    ): String? {
        return try {
            if (secretField == null) {
                return null
            }

            when (secretField) {
                is SecretField.Encrypted -> {
                    when (securityType) {
                        SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(secretField.value)
                        SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(secretField.value)
                        SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(secretField.value)
                    }
                }

                is SecretField.ClearText -> {
                    secretField.value
                }
            }
        } catch (_: VaultKeysExpiredException) {
            null
        }
    }

    private fun Item.asEncrypted(content: EncryptedBytes): ItemEncrypted {
        return ItemEncrypted(
            id = id,
            vaultId = vaultId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            deleted = deleted,
            securityType = securityType,
            contentType = contentType,
            content = content,
            tagIds = tagIds,
        )
    }

    private fun ItemEncrypted.asDecrypted(content: ItemContent): Item {
        return Item(
            id = id,
            vaultId = vaultId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            deleted = deleted,
            securityType = securityType,
            contentType = contentType,
            content = content,
            tagIds = tagIds,
        )
    }

    fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent,
    ): ItemContent {
        return mapperDelegate.decryptSecretFields(vaultCipher, securityType, content)
    }

    fun encryptSecretFields(
        content: ItemContent,
        encryptionKey: ByteArray,
    ): ItemContent {
        return mapperDelegate.encryptSecretFields(content, encryptionKey)
    }
}