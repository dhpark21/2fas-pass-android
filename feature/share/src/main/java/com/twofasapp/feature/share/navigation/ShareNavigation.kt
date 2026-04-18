/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.share.ui.sharelinkhandler.ShareLinkHandlerScreen

@Composable
fun ShareLinkHandlerRoute(
    onDecrypted: (shareId: String, itemContentTypeKey: String) -> Unit,
    close: () -> Unit,
) {
    ShareLinkHandlerScreen(
        onDecrypted = onDecrypted,
        close = close,
    )
}