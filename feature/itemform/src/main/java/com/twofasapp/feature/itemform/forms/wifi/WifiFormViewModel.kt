package com.twofasapp.feature.itemform.forms.wifi

import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.parseWifiQr
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.itemform.ItemFormViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class WifiFormViewModel(
    vaultsRepository: VaultsRepository,
    settingsRepository: SettingsRepository,
    tagsRepository: TagsRepository,
) : ItemFormViewModel<ItemContent.Wifi>(
    vaultsRepository = vaultsRepository,
    settingsRepository = settingsRepository,
    tagsRepository = tagsRepository,
) {

    val passwordVisibilityState = MutableStateFlow(false)
    val wifiSecurityTypeDropdownVisibilityState = MutableStateFlow(false)

    fun onNameChanged(name: String) {
        updateItemContent { content -> content.copy(name = name) }
    }

    fun onPasswordChanged(password: String) {
        updateItemContent { content -> content.copy(password = SecretField.ClearText(password)) }
    }

    fun onHiddenChanged(hidden: Boolean) {
        updateItemContent { content -> content.copy(hidden = hidden) }
    }

    fun onWifiSecurityTypeChanged(securityType: WifiSecurityType) {
        wifiSecurityTypeDropdownVisibilityState.update { false }
        updateItemContent { content -> content.copy(securityType = securityType) }
    }

    fun onWifiSecurityTypeClicked() {
        wifiSecurityTypeDropdownVisibilityState.update { true }
    }

    fun onWifiSecurityTypeDropdownDismissed() {
        wifiSecurityTypeDropdownVisibilityState.update { false }
    }

    fun onSsidChanged(ssid: String) {
        updateItemContent { content -> content.copy(ssid = ssid) }
    }

    fun onNotesChanged(notes: String) {
        updateItemContent { content -> content.copy(notes = notes) }
    }

    fun onPasswordToggleClicked() {
        passwordVisibilityState.update { it.not() }
    }

    fun onQrCodeScanned(qrCode: String): Boolean {
        val scannedContent = ItemContent.Wifi.parseWifiQr(qrCode) ?: return false
        updateItemContent { content ->
            content.copy(
                ssid = scannedContent.ssid,
                securityType = scannedContent.securityType,
                password = scannedContent.password
            )
        }
        return true
    }
}