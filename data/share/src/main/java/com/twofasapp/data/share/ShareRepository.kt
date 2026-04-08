package com.twofasapp.data.share

import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.data.share.domain.ShareLink

interface ShareRepository {
    suspend fun createShareLink(
        item: Item,
        expirationTimeSeconds: Int,
        oneTimeAccess: Boolean,
        password: String?,
    ): ShareLink

    suspend fun decryptShareLink(
        shareId: String,
        version: String,
        nonce: String,
        key: String,
        password: String? = null,
    ): Item

    fun cacheDecryptedShareItem(shareId: String, item: Item)

    fun getDecryptedShareItem(shareId: String): Item?

    fun removeDecryptedShareItem(shareId: String)
}