package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.local.model.items.LoginContentEntityV1
import kotlinx.serialization.json.Json

class LoginEncryptionMapperStrategy(
    private val json: Json,
    private val iconTypeMapper: IconTypeMapper,
    private val uriMapper: ItemUriMapper,
) : ItemEncryptionMapperStrategy<ItemContent.Login> {

    override fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
        contentEntityJson: String,
    ): ItemContent.Login {
        val contentEntity =
            json.decodeFromString(LoginContentEntityV1.serializer(), contentEntityJson)
        return ItemContent.Login(
            name = contentEntity.name,
            username = contentEntity.username,
            password = contentEntity.password?.let {
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
            uris = contentEntity.uris.map { uriMapper.mapToDomain(it) },
            iconType = iconTypeMapper.mapToDomainFromEntity(contentEntity.iconType),
            iconUriIndex = contentEntity.iconUriIndex,
            customImageUrl = contentEntity.customImageUrl,
            labelText = contentEntity.labelText,
            labelColor = contentEntity.labelColor,
            notes = contentEntity.notes,
        )
    }

    override fun encryptItem(
        item: Item,
        content: ItemContent.Login,
        vaultCipher: VaultCipher,
    ): String {
        return json.encodeToString(
            LoginContentEntityV1(
                name = content.name,
                username = content.username,
                password = when (content.password) {
                    is SecretField.Encrypted -> (content.password as SecretField.Encrypted).value
                    is SecretField.ClearText -> {
                        if (content.password.clearText.isBlank()) {
                            null
                        } else {
                            when (item.securityType) {
                                SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.password.clearText)
                                SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.password.clearText)
                                SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.password.clearText)
                            }
                        }
                    }

                    null -> null
                },
                uris = content.uris.map { uriMapper.mapToEntity(it) },
                iconType = iconTypeMapper.mapToEntity(content.iconType),
                iconUriIndex = content.iconUriIndex,
                customImageUrl = content.customImageUrl,
                labelText = content.labelText,
                labelColor = content.labelColor,
                notes = content.notes,
            ),
        )
    }

    override fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent.Login,
    ): ItemContent.Login {
        return content.copy(
            password = content.password?.let {
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
        content: ItemContent.Login,
        encryptionKey: ByteArray,
    ): ItemContent.Login {
        return content.copy(
            password = content.password?.let {
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