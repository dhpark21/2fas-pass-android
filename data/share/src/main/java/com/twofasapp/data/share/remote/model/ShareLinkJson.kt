package com.twofasapp.data.share.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShareLinkJson(
    @SerialName("id")
    val id: String,
)