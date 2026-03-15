/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.enumPref
import com.twofasapp.core.network.ApiEnvironment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class DeveloperRepositoryImpl(
    dataStoreOwner: DataStoreOwner,
    private val dispatchers: Dispatchers,
) : DeveloperRepository, DataStoreOwner by dataStoreOwner {

    private val apiEnvironment by enumPref(
        cls = ApiEnvironment::class.java,
        default = ApiEnvironment.Production,
    )

    override fun observeApiEnvironment(): Flow<ApiEnvironment> {
        return apiEnvironment.asFlow()
    }

    override suspend fun setApiEnvironment(environment: ApiEnvironment) {
        withContext(dispatchers.io) {
            apiEnvironment.set(environment)
        }
    }
}