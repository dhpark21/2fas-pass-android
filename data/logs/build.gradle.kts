/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

plugins {
    alias(libs.plugins.twofasAndroidLibrary)
    alias(libs.plugins.twofasLint)
}

android {
    namespace = "com.twofasapp.data.logs"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:di"))

    implementation(libs.bundles.room)
    implementation(libs.kotlinCoroutines)
}
