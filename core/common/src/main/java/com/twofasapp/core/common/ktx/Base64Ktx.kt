/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

@file:OptIn(ExperimentalEncodingApi::class)

package com.twofasapp.core.common.ktx

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun ByteArray.encodeBase64(): String {
    return Base64.encode(this)
}

fun EncryptedBytes.encodeBase64(): String {
    return Base64.encode(this.bytes)
}

fun ByteArray.encodeBase64UrlSafe(): String {
    return Base64.UrlSafe.encode(this)
        .replace("+", "-")
        .replace("/", "_")
        .replace("=", "")
}

fun EncryptedBytes.encodeBase64UrlSafe(): String {
    return Base64.UrlSafe.encode(this.bytes)
}

fun String.decodeBase64(): ByteArray {
    return Base64.decode(this)
}

fun String.decodeBase64UrlSafe(): ByteArray {
    val padded = when (length % 4) {
        2 -> "$this=="
        3 -> "$this="
        else -> this
    }
    return Base64.UrlSafe.decode(padded)
}

fun String.decodeBase64ToString(): String {
    return String(Base64.decode(this))
}

fun String.encodeUrlParam(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}

fun String.decodeUrlParam(): String {
    return URLDecoder.decode(this, StandardCharsets.UTF_8.toString()).replace(" ", "+")
}