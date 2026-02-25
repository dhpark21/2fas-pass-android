/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails

import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ItemDetailsModalUiState(
    val item: Item = Item.Empty,
    val tags: ImmutableList<Tag> = persistentListOf(),
    val decryptedFields: Map<SecretFieldType, String> = emptyMap(),
)

internal enum class SecretFieldType {
    LoginPassword,
    SecureNoteText,
    PaymentCardNumber,
    PaymentCardExpiration,
    PaymentCardSecurityCode,
    WifiPassword,
    WifiQrPassword,
    WifiConnectPassword,
}