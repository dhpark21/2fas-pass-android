package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.VaultCipher

interface ItemEncryptionMapperStrategy<T : ItemContent> {

    fun decryptItem(
        itemEncrypted: ItemEncrypted,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
        contentEntityJson: String
    ): T

    fun encryptItem(
        item: Item,
        content: T,
        vaultCipher: VaultCipher,
    ): String

    fun decryptSecretFields(
        vaultCipher: VaultCipher,
        securityType: SecurityType,
        content: T,
    ): T

    fun encryptSecretFields(
        content: T,
        encryptionKey: ByteArray,
    ): T
}