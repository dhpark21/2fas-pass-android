package com.twofasapp.core.common.domain

sealed interface WifiSecurityType {
    val value: String

    data object None : WifiSecurityType {
        override val value = "none"
    }

    data object Wep : WifiSecurityType {
        override val value = "wep"
    }

    data object Wpa : WifiSecurityType {
        override val value = "wpa"
    }

    data object Wpa2 : WifiSecurityType {
        override val value = "wpa2"
    }

    data object Wpa3 : WifiSecurityType {
        override val value = "wpa3"
    }

    data class Unknown(override val value: String) : WifiSecurityType

    companion object {
        fun values() = listOf(
            None,
            Wep,
            Wpa,
            Wpa2,
            Wpa3,
        )

        fun fromValue(value: String?): WifiSecurityType {
            return values().firstOrNull { it.value == value } ?: Unknown(value ?: "")
        }
    }
}