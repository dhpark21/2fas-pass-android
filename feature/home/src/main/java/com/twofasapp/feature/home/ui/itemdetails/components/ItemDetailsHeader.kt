package com.twofasapp.feature.home.ui.itemdetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemImage
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.locale.MdtLocale
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ItemDetailsHeader(
    tags: ImmutableList<Tag>,
    item: Item,
    subtitle: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ItemImage(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            item = item,
            size = 50.dp,
        )

        Text(
            text = item.content.name.ifEmpty { MdtLocale.strings.loginNoItemName },
            style = MdtTheme.typo.medium.lg.copy(lineHeight = 22.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .padding(bottom = if (subtitle != null) 0.dp else 12.dp),
            textAlign = TextAlign.Center,
        )

        subtitle?.let {
            Text(
                text = subtitle,
                style = MdtTheme.typo.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Center,
                color = MdtTheme.color.onSurface,
            )
        }

        ItemDetailsPills(item = item, tags = tags)

        Space(12.dp)
    }
}