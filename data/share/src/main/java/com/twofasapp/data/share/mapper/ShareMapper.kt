package com.twofasapp.data.share.mapper

import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.data.share.domain.ShareItem
import com.twofasapp.data.share.domain.ShareItemContent
import kotlinx.serialization.json.Json

internal class ShareMapper(
    private val json: Json,
) {

    fun map(item: Item): ShareItem {
        val type = item.contentType
        return when (val content = item.content) {
            is ItemContent.Login -> mapLogin(type, content)
            is ItemContent.SecureNote -> mapSecureNote(type, content)
            is ItemContent.PaymentCard -> mapPaymentCard(type, content)
            is ItemContent.Wifi -> mapWifi(type, content)
            is ItemContent.Unknown -> mapCustom(type, content)
        }
    }

    private fun mapLogin(type: ItemContentType, content: ItemContent.Login): ShareItem {
        return ShareItem(
            contentVersion = type.version,
            contentType = type.key,
            content = json.encodeToJsonElement(
                ShareItemContent.Login.serializer(),
                ShareItemContent.Login(
                    name = content.name,
                    username = content.username,
                    password = content.password.clearTextOrNull,
                    notes = content.notes,
                    uris = content.uris.map { uri ->
                        ShareItemContent.Login.Uri(
                            uri = uri.text,
                            match = mapUriMatcher(uri.matcher),
                        )
                    }.ifEmpty { null },
                ),
            ),
        )
    }

    private fun mapSecureNote(type: ItemContentType, content: ItemContent.SecureNote): ShareItem {
        return ShareItem(
            contentVersion = type.version,
            contentType = type.key,
            content = json.encodeToJsonElement(
                ShareItemContent.SecureNote.serializer(),
                ShareItemContent.SecureNote(
                    name = content.name,
                    text = content.text.clearTextOrNull,
                ),
            ),
        )
    }

    private fun mapPaymentCard(type: ItemContentType, content: ItemContent.PaymentCard): ShareItem {
        return ShareItem(
            contentVersion = type.version,
            contentType = type.key,
            content = json.encodeToJsonElement(
                ShareItemContent.PaymentCard.serializer(),
                ShareItemContent.PaymentCard(
                    name = content.name,
                    cardHolder = content.cardHolder,
                    cardNumber = content.cardNumber.clearTextOrNull,
                    expirationDate = content.expirationDate.clearTextOrNull,
                    securityCode = content.securityCode.clearTextOrNull,
                    notes = content.notes,
                ),
            ),
        )
    }

    private fun mapWifi(type: ItemContentType, content: ItemContent.Wifi): ShareItem {
        return ShareItem(
            contentVersion = type.version,
            contentType = type.key,
            content = json.encodeToJsonElement(
                ShareItemContent.Wifi.serializer(),
                ShareItemContent.Wifi(
                    name = content.name,
                    ssid = content.ssid,
                    password = content.password.clearTextOrNull,
                    notes = content.notes,
                    securityType = content.securityType.value,
                    hidden = content.hidden,
                ),
            ),
        )
    }

    private fun mapCustom(type: ItemContentType, content: ItemContent.Unknown): ShareItem {
        return ShareItem(
            contentVersion = type.version,
            contentType = type.key,
            content = json.encodeToJsonElement(
                ShareItemContent.Custom.serializer(),
                ShareItemContent.Custom(text = content.rawJson),
            ),
        )
    }

    fun map(shareItem: ShareItem): Item {
        val contentType = ItemContentType.fromKey(shareItem.contentType)
        val content = when (contentType) {
            is ItemContentType.Login -> mapLogin(shareItem)
            is ItemContentType.SecureNote -> mapSecureNote(shareItem)
            is ItemContentType.PaymentCard -> mapPaymentCard(shareItem)
            is ItemContentType.Wifi -> mapWifi(shareItem)
            is ItemContentType.Unknown -> mapCustom(shareItem)
        }
        return Item.create(
            contentType = contentType,
            content = content,
        )
    }

    private fun mapLogin(shareItem: ShareItem): ItemContent.Login {
        val content = json.decodeFromJsonElement(ShareItemContent.Login.serializer(), shareItem.content)
        return ItemContent.Login(
            name = content.name,
            username = content.username,
            password = content.password?.let { SecretField.ClearText(it) },
            notes = content.notes,
            uris = content.uris?.map { uri ->
                ItemUri(
                    text = uri.uri,
                    matcher = mapUriMatcher(uri.match),
                )
            }.orEmpty(),
            iconType = IconType.Icon,
            iconUriIndex = 0,
            customImageUrl = null,
            labelText = null,
            labelColor = null,
        )
    }

    private fun mapSecureNote(shareItem: ShareItem): ItemContent.SecureNote {
        val content = json.decodeFromJsonElement(ShareItemContent.SecureNote.serializer(), shareItem.content)
        return ItemContent.SecureNote(
            name = content.name,
            text = content.text?.let { SecretField.ClearText(it) },
            additionalInfo = null,
        )
    }

    private fun mapPaymentCard(shareItem: ShareItem): ItemContent.PaymentCard {
        val content = json.decodeFromJsonElement(ShareItemContent.PaymentCard.serializer(), shareItem.content)
        return ItemContent.PaymentCard(
            name = content.name,
            cardHolder = content.cardHolder,
            cardNumber = content.cardNumber?.let { SecretField.ClearText(it) },
            cardNumberMask = null,
            expirationDate = content.expirationDate?.let { SecretField.ClearText(it) },
            securityCode = content.securityCode?.let { SecretField.ClearText(it) },
            cardIssuer = null,
            notes = content.notes,
        )
    }

    private fun mapWifi(shareItem: ShareItem): ItemContent.Wifi {
        val content = json.decodeFromJsonElement(ShareItemContent.Wifi.serializer(), shareItem.content)
        return ItemContent.Wifi(
            name = content.name,
            ssid = content.ssid,
            password = content.password?.let { SecretField.ClearText(it) },
            notes = content.notes,
            securityType = WifiSecurityType.fromValue(content.securityType),
            hidden = content.hidden ?: false,
        )
    }

    private fun mapCustom(shareItem: ShareItem): ItemContent.Unknown {
        val content = json.decodeFromJsonElement(ShareItemContent.Custom.serializer(), shareItem.content)
        return ItemContent.Unknown(rawJson = content.text)
    }

    private fun mapUriMatcher(matcher: UriMatcher): String {
        return when (matcher) {
            UriMatcher.Domain -> "domain"
            UriMatcher.Host -> "host"
            UriMatcher.StartsWith -> "startsWith"
            UriMatcher.Exact -> "exact"
        }
    }

    private fun mapUriMatcher(match: String?): UriMatcher {
        return when (match) {
            "domain" -> UriMatcher.Domain
            "host" -> UriMatcher.Host
            "startsWith" -> UriMatcher.StartsWith
            "exact" -> UriMatcher.Exact
            else -> UriMatcher.Domain
        }
    }
}