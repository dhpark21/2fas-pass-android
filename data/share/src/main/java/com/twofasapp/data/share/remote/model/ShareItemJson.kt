package com.twofasapp.data.share.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShareItemJson(
    @SerialName("data")
    val data: String,
    @SerialName("validForSeconds")
    val validForSeconds: Int,
    @SerialName("singleUse")
    val singleUse: Boolean,
)