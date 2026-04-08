/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.logger

interface FlogSink {
    val debug: Boolean

    fun log(
        level: FlogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        persist: Boolean = false,
    )
}