package com.twofasapp.feature.home.ui.itemdetails.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.qrCodeContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.WifiItemContentPreview
import com.twofasapp.core.design.feature.items.WifiItemPreview
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ZeroPadding
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.qr.QrCode
import com.twofasapp.core.design.foundation.text.secretAnnotatedString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.passwordColorized
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.SecretFieldType
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsEntry
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsHeader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ColumnScope.WifiContent(
    item: Item,
    tags: ImmutableList<Tag>,
    content: ItemContent.Wifi,
    decryptedFields: Map<SecretFieldType, String>,
    onToggleSecretField: (SecretFieldType, SecretField?) -> Unit,
    onCopySecretField: (SecretField?, (String) -> Unit) -> Unit,
) {
    val context = LocalContext.current

    var showWifiQrCodeModal by remember { mutableStateOf(false) }

    if (showWifiQrCodeModal) {
        WifiQrCodeModal(
            content = content.qrCodeContent(
                if (content.password == null) {
                    content.qrCodeContent(null)
                } else {
                    decryptedFields[SecretFieldType.WifiQrPassword]?.let {
                        content.qrCodeContent(it)
                    }
                }
            ),
            onDismissRequest = { showWifiQrCodeModal = false },
        )
    }

    ItemDetailsHeader(
        item = item,
        tags = tags,
        subtitle = MdtLocale.strings.wifiFieldHiddenValue.takeIf { content.hidden }
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedShape12),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        ItemDetailsEntry(
            title = MdtLocale.strings.wifiNetworkHeader,
            subtitle = content.name,
            actions = {
                IconButton(
                    icon = MdtIcons.Copy,
                    onClick = { context.copyToClipboard(content.name) },
                )
            },
        )

        content.password?.let { password ->
            ItemDetailsEntry(
                title = MdtLocale.strings.wifiPasswordLabel,
                subtitleAnnotated = decryptedFields[SecretFieldType.WifiPassword]?.let {
                    passwordColorized(password = it)
                } ?: secretAnnotatedString(),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.WifiPassword] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.WifiPassword, password) },
                    )

                    IconButton(
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(password) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        ItemDetailsEntry(
            title = MdtLocale.strings.wifiSecurityTypeLabel,
            subtitle = content.securityType?.value.orEmpty(),
            actions = {
                IconButton(
                    icon = MdtIcons.Copy,
                    onClick = { context.copyToClipboard(content.securityType?.value.orEmpty()) },
                )
            },
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    OptionEntry(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainerHigh)
            .clickable {
                content.password?.let {
                    onToggleSecretField(SecretFieldType.WifiQrPassword, it)
                }
                showWifiQrCodeModal = true
            }
            .padding(16.dp),
        external = true,
        externalIcon = MdtIcons.ChevronRight,
        title = MdtLocale.strings.wifiShowQrCodeAction,
        contentPadding = ZeroPadding,
    )
}

@Composable
private fun WifiQrCodeModal(
    content: String,
    onDismissRequest: () -> Unit,
) {
    Modal(
        headerText = MdtLocale.strings.wifiQrScanTitle,
        onDismissRequest = onDismissRequest,
    ) {
        QrCode(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            content = content
        )
    }
}

@Preview
@Composable
private fun WifiContentPreview() {
    PreviewTheme {
        Column {
            WifiContent(
                item = WifiItemPreview,
                tags = persistentListOf(),
                WifiItemContentPreview.copy(hidden = true),
                decryptedFields = emptyMap(),
                onCopySecretField = { _, _ -> },
                onToggleSecretField = { _, _ -> }
            )
        }
    }
}
