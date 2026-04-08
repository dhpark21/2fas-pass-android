/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.deeplinks

import android.app.Activity
import android.content.Intent
import com.twofasapp.core.android.deeplinks.Deeplink
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.navigation.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

internal class DeeplinksHandler : Deeplinks {

    companion object {
        val Tag = "DeeplinksHandler"
    }

    private val pendingDeeplinkFlow = MutableStateFlow<Deeplink?>(null)

    override suspend fun onCreate(activity: Activity, intent: Intent) {
        try {
            handleIncomingIntent(activity, intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun onNewIntent(activity: Activity, intent: Intent) {
        try {
            handleIncomingIntent(activity, intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observePendingDeeplink(): Flow<Deeplink> {
        return pendingDeeplinkFlow.filterNotNull()
    }

    override fun clearPendingDeeplink() {
        pendingDeeplinkFlow.tryEmit(null)
    }

    override fun openScreen(screen: Screen) {
        publishDeeplink(Deeplink.ToScreen(screen))
    }

    override fun openScreens(screens: List<Screen>) {
        publishDeeplink(Deeplink.ToScreen(screens))
    }

    private fun publishDeeplink(deeplink: Deeplink) {
        pendingDeeplinkFlow.tryEmit(deeplink)
    }

    private fun handleIncomingIntent(activity: Activity, intent: Intent) {
        val uri = intent.data
        Timber.tag(Tag).d("Open: $uri")

        if (uri == null) return

        if (uri.scheme == "twofaspass" && uri.host == "share") {
            val segments = uri.pathSegments
            val (shareId, version, nonce, key) = segments

            if (version != "v1k" && version != "v1p") {
                return
            }

            publishDeeplink(
                Deeplink.ShareLink(
                    shareId = shareId,
                    version = version,
                    nonce = nonce,
                    key = key,
                ),
            )
        }
    }
}