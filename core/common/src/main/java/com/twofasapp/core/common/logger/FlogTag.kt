/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.logger

class FlogTag internal constructor(
    private val tag: String,
) {
    fun v(message: String, persist: Boolean = false) = Flog.dispatch(FlogLevel.Verbose, tag, message, persist = persist)

    fun d(message: String, persist: Boolean = false) = Flog.dispatch(FlogLevel.Debug, tag, message, persist = persist)

    fun i(message: String, persist: Boolean = false) = Flog.dispatch(FlogLevel.Info, tag, message, persist = persist)

    fun w(message: String, persist: Boolean = false) = Flog.dispatch(FlogLevel.Warn, tag, message, persist = persist)

    fun e(message: String, throwable: Throwable? = null, persist: Boolean = false) = Flog.dispatch(FlogLevel.Error, tag, message, throwable, persist)

    fun e(throwable: Throwable?, persist: Boolean = false) = Flog.dispatch(FlogLevel.Error, tag, throwable?.message ?: "", throwable, persist)
}