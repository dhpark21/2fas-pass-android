/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.coroutines

import com.twofasapp.core.common.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher

class AppDispatchers : Dispatchers {
    override val io: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
    override val main: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Main
}