package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.local.model.items.WifiContentEntityV1
import kotlinx.serialization.json.Json

class WifiEncryptionMapperStrategy(
    private val json: Json,
) : ItemEncryptionMapperStrategy<ItemContent.Wifi> {

    override fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
        contentEntityJson: String,
    ): ItemContent.Wifi {
        val contentEntity =
            json.decodeFromString(WifiContentEntityV1.serializer(), contentEntityJson)
        return ItemContent.Wifi(
            name = contentEntity.name,
            ssid = contentEntity.ssid,
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
            securityType = WifiSecurityType.fromValue(contentEntity.securityType),
            hidden = contentEntity.hidden,
            notes = contentEntity.notes,
        )
    }

    override fun encryptItem(
        item: Item,
        content: ItemContent.Wifi,
        vaultCipher: VaultCipher,
    ): String {
        return json.encodeToString(
            WifiContentEntityV1(
                name = content.name,
                ssid = content.ssid,
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
                securityType = content.securityType?.value,
                hidden = content.hidden,
                notes = content.notes,
            ),
        )
    }

    override fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent.Wifi,
    ): ItemContent.Wifi {
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
        content: ItemContent.Wifi,
        encryptionKey: ByteArray,
    ): ItemContent.Wifi {
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