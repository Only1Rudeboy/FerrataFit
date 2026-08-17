plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
}

// Build-Output auf das schnelle Linux-Dateisystem auslagern (WSL: /mnt/c ist zaeh).
// Der Quellcode bleibt hier in Documents, nur die Zwischenergebnisse wandern aus.
val fastBuildRoot: String? = System.getenv("FERRATAFIT_BUILD_DIR")
if (fastBuildRoot != null) {
    allprojects {
        layout.buildDirectory.set(file("$fastBuildRoot/${project.name}"))
    }
}
