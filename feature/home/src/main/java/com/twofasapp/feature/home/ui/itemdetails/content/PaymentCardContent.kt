/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.text.secretString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.SecretFieldType
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsEntry
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsHeader
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun PaymentCardContent(
    item: Item,
    tags: ImmutableList<Tag>,
    content: ItemContent.PaymentCard,
    decryptedFields: Map<SecretFieldType, String>,
    onToggleSecretField: (SecretFieldType, SecretField?) -> Unit,
    onCopySecretField: (SecretField?, (String) -> Unit) -> Unit,
) {
    val context = LocalContext.current

    ItemDetailsHeader(
        item = item,
        tags = tags,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedShape12),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        if (content.cardHolder.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.cardHolderLabel,
                subtitle = content.cardHolder,
                actions = {
                    IconButton(
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.cardHolder.orEmpty()) },
                    )
                },
            )
        }

        content.cardNumber?.let { cardNumber ->
            ItemDetailsEntry(
                title = MdtLocale.strings.cardNumberLabel,
                subtitle = decryptedFields[SecretFieldType.PaymentCardNumber] ?: content.cardNumberMaskDisplayShort,
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.PaymentCardNumber] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.PaymentCardNumber, cardNumber) },
                    )

                    IconButton(
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(cardNumber) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        content.expirationDate?.let { expirationDate ->
            ItemDetailsEntry(
                title = MdtLocale.strings.cardExpirationDateLabel,
                subtitle = decryptedFields[SecretFieldType.PaymentCardExpiration] ?: secretString(count = 5),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.PaymentCardExpiration] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.PaymentCardExpiration, expirationDate) },
                    )

                    IconButton(
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(expirationDate) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        content.securityCode?.let { securityCode ->
            ItemDetailsEntry(
                title = MdtLocale.strings.cardSecurityCodeLabel,
                subtitle = decryptedFields[SecretFieldType.PaymentCardSecurityCode] ?: secretString(count = 3),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.PaymentCardSecurityCode] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.PaymentCardSecurityCode, securityCode) },
                    )

                    IconButton(
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(securityCode) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        if (content.notes.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.loginNotes,
                subtitle = content.notes.orEmpty(),
                isCompact = true,
                actions = {
                    IconButton(
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                    )
                },
            )
        }
    }
}