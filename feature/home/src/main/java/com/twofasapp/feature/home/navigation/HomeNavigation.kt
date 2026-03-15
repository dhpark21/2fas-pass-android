/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.feature.home.ui.editItem.EditItemScreen
import com.twofasapp.feature.home.ui.home.HomeScreen
import com.twofasapp.feature.home.ui.itemdetails.ItemDetailsScreen

@Composable
fun HomeRoute(
    openAddItem: (vaultId: String, itemContentType: ItemContentType) -> Unit,
    openEditItem: (itemId: String, vaultId: String, itemContentType: ItemContentType) -> Unit,
    openItemDetails: (itemId: String, vaultId: String) -> Unit,
    openManageTags: () -> Unit,
    openQuickSetup: () -> Unit,
    openDeveloper: () -> Unit,
    onHomeInEditModeChanged: (Boolean) -> Unit,
    onHomeScrollingUpChanged: (Boolean) -> Unit,
) {
    HomeScreen(
        openAddItem = openAddItem,
        openEditItem = openEditItem,
        openItemDetails = openItemDetails,
        openManageTags = openManageTags,
        openQuickSetup = openQuickSetup,
        openDeveloper = openDeveloper,
        onHomeInEditModeChanged = onHomeInEditModeChanged,
        onHomeScrollingUpChanged = onHomeScrollingUpChanged,
    )
}

@Composable
fun EditItemRoute(
    close: () -> Unit,
) {
    EditItemScreen(
        close = close,
    )
}

@Composable
fun ItemDetailsRoute(
    openEditItem: (itemId: String, vaultId: String, itemContentType: ItemContentType) -> Unit,
    close: () -> Unit,
) {
    ItemDetailsScreen(openEditItem = openEditItem, close = close)
}