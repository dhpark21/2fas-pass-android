/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.logs.domain

data class LogEntry(
    val id: Long,
    val tag: String,
    val timestamp: Long,
    val message: String,
)