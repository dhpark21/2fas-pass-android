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
}