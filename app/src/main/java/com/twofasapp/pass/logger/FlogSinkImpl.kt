/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.logger

import android.util.Log
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.logger.FlogLevel
import com.twofasapp.core.common.logger.FlogSink
import com.twofasapp.data.logs.LogsRepository
import timber.log.Timber

class FlogSinkImpl(
    appBuild: AppBuild,
    private val logsRepository: LogsRepository,
) : FlogSink {

    override val debug: Boolean = appBuild.buildVariant == BuildVariant.Debug

    init {
        if (debug) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun log(level: FlogLevel, tag: String, message: String, throwable: Throwable?, persist: Boolean) {
        val priority = when (level) {
            FlogLevel.Verbose -> Log.VERBOSE
            FlogLevel.Debug -> Log.DEBUG
            FlogLevel.Info -> Log.INFO
            FlogLevel.Warn -> Log.WARN
            FlogLevel.Error -> Log.ERROR
        }

        Timber.tag(tag).log(priority, throwable, message)

        if (persist) {
            logsRepository.save(level, message)
        }
    }
}