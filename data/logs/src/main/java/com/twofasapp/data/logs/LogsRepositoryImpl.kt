/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.logs

import com.twofasapp.data.logs.domain.LogEntry
import com.twofasapp.data.logs.local.LogsLocalSource
import com.twofasapp.data.logs.local.model.LogEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class LogsRepositoryImpl(
    private val localSource: LogsLocalSource,
) : LogsRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun save(tag: String, message: String) {
        scope.launch {
            localSource.insert(
                LogEntryEntity(
                    tag = tag,
                    timestamp = System.currentTimeMillis(),
                    message = message,
                ),
            )
        }
    }

    override suspend fun getAll(): List<LogEntry> {
        return localSource.getAll().map { entity ->
            LogEntry(
                id = entity.id,
                tag = entity.tag,
                timestamp = entity.timestamp,
                message = entity.message,
            )
        }
    }

    override suspend fun deleteAll() {
        localSource.deleteAll()
    }
}