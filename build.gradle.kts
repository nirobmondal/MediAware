// Top-level build file — plugin declarations only.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

// Redirect build outputs from NTFS partition to native Linux ext4 filesystem
// to prevent Linux ntfs3 kernel lockups during parallel file unlinking
allprojects {
    val moduleName = if (path == ":") "root" else path.removePrefix(":").replace(":", "_")
    layout.buildDirectory.set(file("/home/nirob202/.cache/mediaware-build/$moduleName"))
}