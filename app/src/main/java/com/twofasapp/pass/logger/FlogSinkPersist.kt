/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.logger

import com.twofasapp.core.common.logger.FlogLevel
import com.twofasapp.core.common.logger.FlogSink
import com.twofasapp.data.logs.LogsRepository

class FlogSinkPersist(
    private val logsRepository: LogsRepository,
) : FlogSink {

    override fun log(level: FlogLevel, tag: String, message: String, throwable: Throwable?) {
        logsRepository.save(
            tag = tag,
            message = if (throwable != null) {
                "${throwable::class.simpleName}: ${throwable.message}\n${throwable.stackTrace.take(3).joinToString("\n") { "  at $it" }}"
            } else {
                message
            },
        )
    }
}