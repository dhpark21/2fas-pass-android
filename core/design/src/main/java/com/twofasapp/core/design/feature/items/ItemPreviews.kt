package com.twofasapp.core.design.feature.items

import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.foundation.preview.PreviewTextLong

fun itemPreview(content: ItemContent): Item {
    return Item.Empty.copy(content = content)
}

val LoginItemContentPreview: ItemContent.Login = ItemContent.Login(
    name = "Login Name",
    username = "user@mail.com",
    password = SecretField.ClearText(""),
    uris = listOf(
        ItemUri("https://2fas.com", UriMatcher.Domain),
        ItemUri("https://google.com", UriMatcher.Host),
    ),
    iconType = IconType.Label,
    iconUriIndex = 0,
    customImageUrl = null,
    labelText = "NA",
    labelColor = "#FF55FF",
    notes = null,
)

val SecureNoteItemContentPreview: ItemContent.SecureNote = ItemContent.SecureNote(
    name = "Secure Note Name",
    text = SecretField.ClearText(PreviewTextLong),
    additionalInfo = null,
)

val PaymentCardItemContentPreview: ItemContent.PaymentCard = ItemContent.PaymentCard(
    name = "Payment Card Name",
    cardHolder = "John Doe",
    cardNumber = SecretField.ClearText("4532123456789012"),
    cardNumberMask = "9012",
    expirationDate = SecretField.ClearText("12/25"),
    securityCode = SecretField.ClearText("123"),
    cardIssuer = ItemContent.PaymentCard.Issuer.Visa,
    notes = "Personal card for online purchases",
)

val WifiItemContentPreview: ItemContent.Wifi = ItemContent.Wifi(
    name = "Wifi",
    ssid = "Wifi ssid",
    password = SecretField.ClearText(""),
    securityType = WifiSecurityType.Wpa2,
    hidden = false,
    notes = null,
)

val LoginItemPreview = itemPreview(LoginItemContentPreview)
val SecureNoteItemPreview = itemPreview(SecureNoteItemContentPreview)
val PaymentCardItemPreview = itemPreview(PaymentCardItemContentPreview)
val WifiItemPreview = itemPreview(WifiItemContentPreview)