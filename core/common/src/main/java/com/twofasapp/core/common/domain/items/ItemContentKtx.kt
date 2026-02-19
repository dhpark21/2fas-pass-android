package com.twofasapp.core.common.domain.items

import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.WifiSecurityType

fun String.formatWithGrouping(grouping: List<Int>): String {
    if (isEmpty()) return this

    val result = StringBuilder()
    var position = 0

    for (groupSize in grouping) {
        if (position >= length) break

        if (result.isNotEmpty()) {
            result.append(" ")
        }

        val endPosition = minOf(position + groupSize, length)
        result.append(substring(position, endPosition))
        position = endPosition

        if (position >= length) break
    }

    if (position < length) {
        if (result.isNotEmpty()) {
            result.append(" ")
        }
        result.append(substring(position))
    }

    return result.toString()
}

fun ItemContent.PaymentCard.Issuer?.cardNumberGrouping(): List<Int> {
    return when (this) {
        ItemContent.PaymentCard.Issuer.AmericanExpress -> listOf(4, 6, 5)
        ItemContent.PaymentCard.Issuer.DinersClub -> listOf(4, 6, 4)
        ItemContent.PaymentCard.Issuer.Visa,
        ItemContent.PaymentCard.Issuer.MasterCard,
        ItemContent.PaymentCard.Issuer.Discover,
        ItemContent.PaymentCard.Issuer.Jcb,
        ItemContent.PaymentCard.Issuer.UnionPay,
        null,
            -> listOf(4, 4, 4, 4, 3)
    }
}

fun ItemContent.Wifi.qrCodeContent(decryptedPassword: String?): String {

    fun String.escape(): String {
        return replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
            .replace("\"", "\\\"")
    }

    return buildString {
        append("WIFI:")
        append("T:")
        append(
            when (securityType) {
                WifiSecurityType.None,
                is WifiSecurityType.Unknown -> "nopass"

                WifiSecurityType.Wep -> "WEP"
                WifiSecurityType.Wpa,
                WifiSecurityType.Wpa2,
                WifiSecurityType.Wpa3 -> "WPA"
            }
        )
        append(";")
        ssid?.let {
            append("S:${it.escape()};")
        }
        decryptedPassword?.let {
            append("P:${it.escape()};")
        }
        append(";")
    }
}

fun ItemContent.Wifi.Companion.parseWifiQr(content: String): ItemContent.Wifi? {

    fun String.unescape(): String {
        val out = StringBuilder()
        var escape = false
        for (c in this) {
            if (escape) {
                out.append(c)
                escape = false
            } else if (c == '\\') {
                escape = true
            } else {
                out.append(c)
            }
        }
        return out.toString()
    }

    if (!content.startsWith("WIFI:")) return null

    val body = content.removePrefix("WIFI:")
        .removeSuffix(";")

    val fields = mutableMapOf<String, String>()

    var current = StringBuilder()
    var escape = false
    val parts = mutableListOf<String>()

    for (c in body) {
        when {
            escape -> {
                current.append(c)
                escape = false
            }

            c == '\\' -> escape = true
            c == ';' -> {
                parts += current.toString()
                current = StringBuilder()
            }

            else -> current.append(c)
        }
    }
    if (current.isNotEmpty()) parts += current.toString()

    parts.forEach { part ->
        val idx = part.indexOf(':')
        if (idx > 0) {
            val key = part.substring(0, idx)
            val value = part.substring(idx + 1)
            fields[key] = value
        }
    }

    val type = when (fields["T"]?.uppercase()) {
        "WEP" -> WifiSecurityType.Wep
        "WPA" -> WifiSecurityType.Wpa2
        else -> WifiSecurityType.None
    }

    val ssid = fields["S"]?.unescape()
    val password = fields["P"]?.unescape()?.let { SecretField.ClearText(it) }
    val hidden = fields["H"]?.equals("true", ignoreCase = true) == true

    return ItemContent.Wifi(
        name = ssid ?: "WiFi",
        ssid = ssid,
        password = password,
        securityType = type,
        hidden = hidden,
        notes = null
    )
}