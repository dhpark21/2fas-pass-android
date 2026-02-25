/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import android.net.Uri
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.core.design.R
import com.twofasapp.core.design.foundation.preview.PreviewTextMedium
import timber.log.Timber
import java.time.Instant

internal abstract class ImportSpec() {
    abstract val type: ImportType
    abstract val name: String
    abstract val image: Int
    abstract val instructions: String
    abstract val additionalInfo: String?
    abstract val cta: List<Cta>

    protected val tags: MutableList<Tag> = mutableListOf()

    abstract suspend fun readContent(uri: Uri): ImportContent

    sealed interface Cta {
        data class Primary(
            val text: String,
            val action: CtaAction,
        ) : Cta
    }

    sealed interface CtaAction {
        data object ChooseFile : CtaAction
    }

    companion object {
        val Empty = object : ImportSpec() {
            override val type: ImportType = ImportType.Bitwarden
            override val name = "Name"
            override val image = R.drawable.ic_android
            override val instructions =
                "$PreviewTextMedium\n\n$PreviewTextMedium\n\n$PreviewTextMedium"
            override val additionalInfo = PreviewTextMedium
            override val cta =
                listOf<Cta>(Cta.Primary(text = "Choose file", action = CtaAction.ChooseFile))

            override suspend fun readContent(uri: Uri): ImportContent =
                ImportContent(emptyList(), emptyList(), 0)
        }
    }

    protected fun resolveTagIds(
        raw: String?,
        vaultId: String,
        separator: Char,
    ): List<String> {
        val names = raw
            .orEmpty()
            .split(separator)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        return names
            .map { name ->
                tags.firstOrNull { it.name == name }
                    ?: Tag.create(
                        vaultId = vaultId,
                        id = Uuid.generate(),
                        name = name,
                    ).also(tags::add)
            }
            .map(Tag::id)
            .toList()
    }

    protected fun detectCardNumberMask(cardNumber: String): String? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.length < 4) return null
        return digitsOnly.takeLast(4)
    }

    protected fun detectCardIssuer(cardNumber: String): ItemContent.PaymentCard.Issuer? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null

        return when {
            digitsOnly.startsWith("4") -> ItemContent.PaymentCard.Issuer.Visa
            digitsOnly.startsWith("5") -> ItemContent.PaymentCard.Issuer.MasterCard
            digitsOnly.startsWith("34") || digitsOnly.startsWith("37") -> ItemContent.PaymentCard.Issuer.AmericanExpress
            digitsOnly.startsWith("6011") || digitsOnly.startsWith("65") -> ItemContent.PaymentCard.Issuer.Discover
            digitsOnly.startsWith("35") -> ItemContent.PaymentCard.Issuer.Jcb
            digitsOnly.startsWith("62") -> ItemContent.PaymentCard.Issuer.UnionPay
            else -> null
        }
    }

    protected fun parseSecondsFrom1970(seconds: Long): Long? {
        return try {
            Instant.ofEpochSecond(seconds).toEpochMilli()
        } catch (t: Throwable) {
            Timber.e(t)
            null
        }
    }

    protected fun parseIsoDate(date: String): Long? {
        if (date.isBlank()) {
            return null
        }
        return try {
            Instant.parse(date).toEpochMilli()
        } catch (t: Throwable) {
            Timber.e(t)
            null
        }
    }

    protected fun parseNetDate(date: String): Long? {
        if (date.isBlank()) {
            return null
        }
        return try {
            val bytes = date.decodeBase64()
            val secondsFromYearOne = (bytes[0].toLong() and 0xFF) or
                ((bytes[1].toLong() and 0xFF) shl 8) or
                ((bytes[2].toLong() and 0xFF) shl 16) or
                ((bytes[3].toLong() and 0xFF) shl 24) or
                ((bytes[4].toLong() and 0xFF) shl 32) or
                ((bytes[5].toLong() and 0xFF) shl 40) or
                ((bytes[6].toLong() and 0xFF) shl 48) or
                ((bytes[7].toLong() and 0xFF) shl 56)
            val secondsBetweenYearOneAnd1970 = 62135596800L
            val secondsFromYear1970 = secondsFromYearOne - secondsBetweenYearOneAnd1970
            Instant.ofEpochSecond(secondsFromYear1970).toEpochMilli()
        } catch (t: Throwable) {
            Timber.e(t)
            null
        }
    }

    protected fun parseWifiSecurityType(securityText: String?): WifiSecurityType {
        if (securityText == null) {
            return WifiSecurityType.Wpa2
        }

        val text = securityText.trim().removeWhitespace().replace(" ", "")

        if (text.equals("unsecured", true)) {
            return WifiSecurityType.None
        }
        if (text.equals("nopass", true)) {
            return WifiSecurityType.None
        }
        if (text == "0") {
            return WifiSecurityType.None
        }
        if (text == "1") {
            return WifiSecurityType.Wep
        }
        if (text == "2") {
            return WifiSecurityType.Wpa
        }
        if (text == "3") {
            return WifiSecurityType.Wpa2
        }
        if (text == "4") {
            return WifiSecurityType.Wpa3
        }
        if (text.contains("wpa2", true)) {
            return WifiSecurityType.Wpa2
        }
        if (text.contains("wpa3", true)) {
            return WifiSecurityType.Wpa3
        }
        if (text.contains("wpa", true)) {
            return WifiSecurityType.Wpa
        }
        val wifiSecurityType = WifiSecurityType.fromValue(text.lowercase())
        if (wifiSecurityType is WifiSecurityType.Unknown) {
            return WifiSecurityType.Wpa2
        }
        return wifiSecurityType
    }

    protected data class ParsedItem(
        val item: Item,
        val tagNames: List<String>,
    ) {
        fun resolve(tags: List<Tag>): Item {
            return item.copy(
                tagIds = tags.filter { tag -> tagNames.contains(tag.name) }.map { it.id },
            )
        }
    }
}