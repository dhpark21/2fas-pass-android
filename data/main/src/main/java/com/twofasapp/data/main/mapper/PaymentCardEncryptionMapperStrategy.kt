package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.data.main.VaultCipher
import com.twofasapp.data.main.local.model.items.PaymentCardContentEntityV1
import kotlinx.serialization.json.Json

class PaymentCardEncryptionMapperStrategy(
    private val json: Json,
) : ItemEncryptionMapperStrategy<ItemContent.PaymentCard> {

    override fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
        contentEntityJson: String,
    ): ItemContent.PaymentCard {
        val contentEntity =
            json.decodeFromString(PaymentCardContentEntityV1.serializer(), contentEntityJson)

        return ItemContent.PaymentCard(
            name = contentEntity.name,
            cardHolder = contentEntity.cardHolder,
            cardNumber = contentEntity.cardNumber?.let {
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
            expirationDate = contentEntity.expirationDate?.let {
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
            securityCode = contentEntity.securityCode?.let {
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
            cardNumberMask = contentEntity.cardNumberMask,
            cardIssuer = ItemContent.PaymentCard.Issuer.fromCode(contentEntity.cardIssuer),
            notes = contentEntity.notes,
        )
    }

    override fun encryptItem(
        item: Item,
        content: ItemContent.PaymentCard,
        vaultCipher: VaultCipher,
    ): String {
        return json.encodeToString(
            PaymentCardContentEntityV1(
                name = content.name,
                cardHolder = content.cardHolder,
                cardNumber = when (content.cardNumber) {
                    is SecretField.Encrypted -> (content.cardNumber as SecretField.Encrypted).value
                    is SecretField.ClearText -> {
                        if (content.cardNumber.clearText.isBlank()) {
                            null
                        } else {
                            when (item.securityType) {
                                SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.cardNumber.clearText)
                                SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.cardNumber.clearText)
                                SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.cardNumber.clearText)
                            }
                        }
                    }

                    null -> null
                },
                expirationDate = when (content.expirationDate) {
                    is SecretField.Encrypted -> (content.expirationDate as SecretField.Encrypted).value
                    is SecretField.ClearText -> {
                        if (content.expirationDate.clearText.isBlank()) {
                            null
                        } else {
                            when (item.securityType) {
                                SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.expirationDate.clearText)
                                SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.expirationDate.clearText)
                                SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.expirationDate.clearText)
                            }
                        }
                    }

                    null -> null
                },
                securityCode = when (content.securityCode) {
                    is SecretField.Encrypted -> (content.securityCode as SecretField.Encrypted).value
                    is SecretField.ClearText -> {
                        if (content.securityCode.clearText.isBlank()) {
                            null
                        } else {
                            when (item.securityType) {
                                SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(content.securityCode.clearText)
                                SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(content.securityCode.clearText)
                                SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(content.securityCode.clearText)
                            }
                        }
                    }

                    null -> null
                },
                cardNumberMask = content.cardNumberMask,
                cardIssuer = content.cardIssuer?.code,
                notes = content.notes,
            ),
        )
    }

    override fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: ItemContent.PaymentCard,
    ): ItemContent.PaymentCard {
        return content.copy(
            cardNumber = content.cardNumber?.let {
                when (it) {
                    is SecretField.ClearText -> it
                    is SecretField.Encrypted -> {
                        SecretField.ClearText(
                            when (securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                            }.removeWhitespace(),
                        )
                    }
                }
            },
            expirationDate = content.expirationDate?.let {
                when (it) {
                    is SecretField.ClearText -> it
                    is SecretField.Encrypted -> {
                        SecretField.ClearText(
                            when (securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                            }.removeWhitespace(),
                        )
                    }
                }
            },
            securityCode = content.securityCode?.let {
                when (it) {
                    is SecretField.ClearText -> it
                    is SecretField.Encrypted -> {
                        SecretField.ClearText(
                            when (securityType) {
                                SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(it.value)
                                SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(it.value)
                            }.removeWhitespace(),
                        )
                    }
                }
            },
        )
    }

    override fun encryptSecretFields(
        content: ItemContent.PaymentCard,
        encryptionKey: ByteArray,
    ): ItemContent.PaymentCard {
        return content.copy(
            cardNumber = content.cardNumber?.let {
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
            expirationDate = content.expirationDate?.let {
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
            securityCode = content.securityCode?.let {
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