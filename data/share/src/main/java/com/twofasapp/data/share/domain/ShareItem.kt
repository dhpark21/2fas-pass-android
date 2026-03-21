package com.twofasapp.data.share.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class ShareItem(
    @SerialName("contentVersion")
    val contentVersion: Int,
    @SerialName("contentType")
    val contentType: String,
    @SerialName("content")
    val content: JsonElement,
)