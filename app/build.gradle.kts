plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "app.xtorayhd"
    compileSdk = 37
    defaultConfig {
        applicationId = "app.xtorayhd"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { compose = true }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation(files("libs/libv2ray.aar"))
}
