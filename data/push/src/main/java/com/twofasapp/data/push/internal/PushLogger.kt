/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.push.internal

import com.google.firebase.messaging.RemoteMessage
import com.twofasapp.core.common.logger.Flog

object PushLogger {
    private const val Tag = "PushMessagingService"

    fun logMessage(remoteMessage: RemoteMessage) {
        try {
            Flog.tag(Tag)
                .i("\uD83D\uDD14 Push Received <= data=${remoteMessage.data}, title=${remoteMessage.notification?.title}, body=${remoteMessage.notification?.body}")
        } catch (e: Exception) {
            Flog.e(e)
        }
    }

    fun logToken(token: String) {
        Flog.tag(Tag).i("FcmToken: $token")
    }

    fun log(message: String) {
        Flog.tag(Tag).i(message)
    }
}