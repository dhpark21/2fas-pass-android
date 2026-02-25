package com.twofasapp.data.main.local.model.items

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.serializers.EncryptedBytesSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WifiContentEntityV1(
    @SerialName("name")
    val name: String,
    @SerialName("ssid")
    val ssid: String?,
    @SerialName("s_password")
    @Serializable(with = EncryptedBytesSerializer::class)
    val password: EncryptedBytes?,
    @SerialName("securityType")
    val securityType: String?,
    @SerialName("hidden")
    val hidden: Boolean,
    @SerialName("notes")
    val notes: String?,
)