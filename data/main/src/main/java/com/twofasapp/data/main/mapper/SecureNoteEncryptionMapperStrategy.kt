package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.local.model.items.SecureNoteContentEntityV1
import kotlinx.serialization.json.Json

class SecureNoteEncryptionMapperStrategy(
    private val json: Json,
) : ItemEncryptionMapperStrategy<ItemContent.SecureNote> {

    override fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
        contentEntityJson: String
    ): ItemContent.SecureNote {
        val contentEntity =
            json.decodeFromString(SecureNoteContentEntityV1.serializer(), contentEntityJson)

        return ItemContent.SecureNote(
            name = contentEntity.name,
            text = contentEntity.text?.let {
                if (decryptSecretFields) {
                    SecretField.ClearText(
                        when (itemEncrypted.securityType) {
                            SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it)
                            SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it)
                            SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it)
                        },
                    )
                } else {
                    SecretField.Encrypted(it)
                }
            },
            additionalInfo = contentEntity.additionalInfo,
        )
    }

    override fun encryptItem(
        item: Item,
        content: ItemContent.SecureNote,
        vaultCipher: VaultCipher
    ): String {
        return json.encodeToString(
            SecureNoteContentEntityV1(
                name = content.name,
                text = when (content.text) {
                    is SecretField.Encrypted -> (content.text as SecretField.Encrypted).value
                    is SecretField.ClearText -> {
                        if (content.text.clearText.isBlank()) {
                            null
                        } else {
                            when (item.securityType) {
                                SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.text.clearText)
                                SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.text.clearText)
                                SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.text.clearText)
                            }
                        }
                    }

                    null -> null
                },
                additionalInfo = content.additionalInfo,
            ),
        )
    }

    override fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent.SecureNote
    ): ItemContent.SecureNote {
        return content.copy(
            text = content.text?.let {
                when (it) {
                    is SecretField.ClearText -> it
                    is SecretField.Encrypted -> {
                        SecretField.ClearText(
                            when (securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                            },
                        )
                    }
                }
            },
        )
    }

    override fun encryptSecretFields(
        content: ItemContent.SecureNote,
        encryptionKey: ByteArray
    ): ItemContent.SecureNote {
        return content.copy(
            text = content.text?.let {
                when (it) {
                    is SecretField.Encrypted -> it
                    is SecretField.ClearText -> {
                        if (it.value.isBlank()) {
                            null
                        } else {
                            SecretField.Encrypted(
                                encrypt(key = encryptionKey, data = it.value),
                            )
                        }
                    }
                }
            },
        )
    }
}