/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.logs.local

import com.twofasapp.data.logs.local.dao.LogsDao
import com.twofasapp.data.logs.local.model.LogEntity

internal class LogsLocalSource(
    private val dao: LogsDao,
) {
    suspend fun insert(entity: LogEntity) = dao.insert(entity)

    suspend fun getAll(): List<LogEntity> = dao.getAll()

    suspend fun deleteAll() = dao.deleteAll()
}