package com.twofasapp.core.design.feature.wifi

import androidx.compose.runtime.Composable
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.locale.MdtLocale

@Composable
fun WifiSecurityType.formatName(): String {
    return when (this) {
        WifiSecurityType.None,
        is WifiSecurityType.Unknown,
        -> MdtLocale.strings.wifiSecurityTypeNone

        WifiSecurityType.Wep,
        WifiSecurityType.Wpa,
        WifiSecurityType.Wpa2,
        WifiSecurityType.Wpa3,
        -> value.uppercase()
    }
}