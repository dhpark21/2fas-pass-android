/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.ui

import androidx.compose.runtime.Composable
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.locale.MdtLocale

internal enum class ShareItemScreenState {
    Form,
    Loading,
    Success,
}

internal data class ShareItemUiState(
    val screenState: ShareItemScreenState = ShareItemScreenState.Form,
    val item: Item = Item.Empty,
    val expirationTime: ExpirationTime = ExpirationTime.Min30,
    val oneTimeAccess: Boolean = false,
    val password: String? = null,
    val link: String = "",
)

internal enum class ExpirationTime(val seconds: Int) {
    Min5(300),
    Min30(1800),
    Hour1(3600),
    Day1(86400),
    Days7(604800),
    Days30(2592000),
    ;

    internal val label: String
        @Composable
        get() {
            val strings = MdtLocale.strings
            return when (this) {
                Min5 -> strings.shareExpiration5Min
                Min30 -> strings.shareExpiration30Min
                Hour1 -> strings.shareExpiration1Hour
                Day1 -> strings.shareExpiration1Day
                Days7 -> strings.shareExpiration7Days
                Days30 -> strings.shareExpiration30Days
            }
        }
}