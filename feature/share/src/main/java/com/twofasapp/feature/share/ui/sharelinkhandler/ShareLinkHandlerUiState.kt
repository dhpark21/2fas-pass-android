/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.ui.sharelinkhandler

internal data class ShareLinkHandlerUiState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val shouldShowPasswordDialog: Boolean = false,
    val shouldClose: Boolean = false,
    val decryptedShareId: String? = null,
    val decryptedItemContentTypeKey: String? = null,
)