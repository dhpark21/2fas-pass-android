/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.keyboardOffsetWithoutNavigationAsState
import com.twofasapp.core.android.ktx.toDp
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.compose.koinInject

@Composable
fun AuthenticationForm(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    cta: String,
    icon: Painter = MdtIcons.Encrypted,
    loading: Boolean = false,
    enabled: Boolean = true,
    biometricsEnabled: Boolean? = null,
    masterKeyEncryptedWithBiometrics: EncryptedBytes? = null,
    passwordError: String? = null,
    showBiometricsOnStart: Boolean = true,
    containerColor: Color = MdtTheme.color.background,
    onUnlockClick: (String) -> Unit = {},
    onMasterKeyDecrypted: (ByteArray) -> Unit = {},
    onBiometricsInvalidated: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    appBuild: AppBuild = koinInject(),
) {
    val strings = MdtLocale.strings
    var password by remember {
        mutableStateOf(
            when (appBuild.buildVariant) {
                BuildVariant.Release -> ""
                BuildVariant.Internal -> ""
                BuildVariant.Debug -> "pass12345"
            },
        )
    }
    var biometricsInvalidated by remember { mutableStateOf(false) }
    val ctaEnabled by remember { derivedStateOf { password.isNotEmpty() } }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val ctaOffset by keyboardOffsetWithoutNavigationAsState()

    if (biometricsEnabled == null) {
        return
    }

    LaunchedEffect(Unit) {
        if (biometricsEnabled.not() || showBiometricsOnStart.not()) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .background(containerColor)
            .padding(horizontal = ScreenPadding)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .padding()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            ScreenHeader(
                title = title,
                description = description,
                icon = icon,
            )

            Space(32.dp)

            MasterPasswordField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                password = password,
                error = passwordError,
                enabled = loading.not() && enabled,
                onPasswordChange = { password = it },
                onDone = {
                    focusManager.clearFocus(true)
                    onUnlockClick(password)
                },
            )

            Space(16.dp)

            MasterBiometricsButton(
                modifier = Modifier.fillMaxWidth(),
                text = strings.authUseBiometrics,
                modalTitle = strings.authBiometricsModalTitle,
                modalSubtitle = title,
                biometricsEnabled = biometricsEnabled && enabled,
                masterKey = masterKeyEncryptedWithBiometrics,
                showOnStart = showBiometricsOnStart,
                onMasterKeyDecrypted = onMasterKeyDecrypted,
                onBiometricsInvalidated = {
                    biometricsInvalidated = true
                    onBiometricsInvalidated()
                },
            )

            if (biometricsInvalidated) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = strings.authBiometricsDisabledMessage,
                    style = MdtTheme.typo.regular.sm,
                    textAlign = TextAlign.Center,
                    color = MdtTheme.color.error,
                )
            }

            Space(1f)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .offset(y = (-ctaOffset.toDp() + 16.dp + 40.dp).coerceAtMost(0.dp)),
        ) {
            Space(8.dp)

            Button(
                modifier = Modifier.fillMaxWidth(),
                text = cta,
                enabled = ctaEnabled && enabled,
                loading = loading,
                onClick = {
                    focusManager.clearFocus(true)
                    onUnlockClick(password)
                },
            )

            Space(16.dp)

            Button(
                modifier = Modifier.fillMaxWidth(),
                style = ButtonStyle.Text,
                text = strings.lockScreenForgotPasswordCta,
                enabled = loading.not() && enabled,
                loading = false,
                onClick = {
                    focusManager.clearFocus(true)
                    onForgotPasswordClick()
                },
            )

            Space(16.dp)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val strings = MdtLocale.strings

    PreviewTheme {
        AuthenticationForm(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(ScreenPadding),
            title = strings.authPreviewTitle,
            description = strings.authPreviewDescription,
            cta = strings.authPreviewCta,
        )
    }
}