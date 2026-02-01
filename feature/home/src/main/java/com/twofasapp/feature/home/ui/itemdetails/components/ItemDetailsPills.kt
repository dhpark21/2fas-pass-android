/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.tags.iconTint
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.feature.home.ui.home.components.securityItemPillColor
import com.twofasapp.feature.itemform.modals.securitytype.asIcon
import com.twofasapp.feature.itemform.modals.securitytype.asTitle
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ItemDetailsPills(item: Item, tags: ImmutableList<Tag>) {
    if (tags.isNotEmpty()) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                8.dp,
                Alignment.CenterHorizontally,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Pill(
                text = item.securityType.asTitle(),
                icon = item.securityType.asIcon(),
                color = securityItemPillColor,
            )
            tags.filter { item.tagIds.contains(it.id) }.forEach { tag ->
                Pill(text = tag.name, icon = MdtIcons.TagFilled, color = tag.iconTint())
            }
        }
    }
}

@Composable
private fun Pill(
    text: String,
    icon: Painter,
    color: Color,
    modifier: Modifier = Modifier,
) {
    TextIcon(
        text = text,
        leadingIcon = icon,
        leadingIconSize = 14.dp,
        leadingIconTint = color,
        color = MdtTheme.color.inverseSurface,
        style = MdtTheme.typo.labelSmall,
        modifier = modifier
            .clip(CircleShape)
            .background(MdtTheme.color.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}