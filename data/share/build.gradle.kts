plugins {
    alias(libs.plugins.twofasAndroidLibrary)
    alias(libs.plugins.twofasLint)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.twofasapp.data.share"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":core:network"))
    implementation(project(":data:main"))

    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinSerialization)
    implementation(libs.bundles.ktor)
}
