/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.logger

object Flog {
    @Volatile private var sink: FlogSink? = null

    fun init(sink: FlogSink) {
        Flog.sink = sink
    }

    fun tag(tag: String): FlogTag = FlogTag(tag)

    fun v(message: String, persist: Boolean = false) = autoTag().v(message, persist)
    fun d(message: String, persist: Boolean = false) = autoTag().d(message, persist)
    fun i(message: String, persist: Boolean = false) = autoTag().i(message, persist)
    fun w(message: String, persist: Boolean = false) = autoTag().w(message, persist)
    fun e(message: String, throwable: Throwable? = null, persist: Boolean = false) = autoTag().e(message, throwable, persist)
    fun e(throwable: Throwable?, persist: Boolean = false) = autoTag().e(throwable, persist)

    internal fun dispatch(level: FlogLevel, tag: String, message: String, throwable: Throwable? = null, persist: Boolean = false) {
        sink?.log(level, tag, message, throwable, persist)
    }

    private fun autoTag(): FlogTag = FlogTag(callerTag())

    private fun callerTag(): String {
        if (sink?.debug != true) return ""
        return Throwable().stackTrace
            .firstOrNull { it.className != Flog::class.java.name }
            ?.className
            ?.substringAfterLast('.')
            ?: ""
    }
}