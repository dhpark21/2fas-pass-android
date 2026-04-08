/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.logs.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.twofasapp.data.logs.local.model.LogEntity

@Dao
interface LogsDao {
    @Insert
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    suspend fun getAll(): List<LogEntity>

    @Query("DELETE FROM logs")
    suspend fun deleteAll()
}