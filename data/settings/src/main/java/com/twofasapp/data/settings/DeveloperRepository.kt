/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings

import com.twofasapp.core.network.ApiEnvironment
import kotlinx.coroutines.flow.Flow

interface DeveloperRepository {
    fun observeApiEnvironment(): Flow<ApiEnvironment>
    suspend fun setApiEnvironment(environment: ApiEnvironment)
}