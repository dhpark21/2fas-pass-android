package com.twofasapp.data.share.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShareSecretJson(
    @SerialName("id")
    val id: String,
    @SerialName("data")
    val data: String,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("validUntil")
    val validUntil: String,
    @SerialName("singleUse")
    val singleUse: Boolean,
)
