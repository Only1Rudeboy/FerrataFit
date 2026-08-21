import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Signatur-Zugangsdaten liegen ausserhalb des Repositories in keystore.properties.
// Fehlt die Datei (z. B. auf einer frisch geklonten Kopie), baut Gradle trotzdem
// durch und nimmt den Debug-Schluessel.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val hasSigningKey = keystoreProps.getProperty("storeFile")
    ?.let { rootProject.file(it).exists() } == true

android {
    namespace = "at.rudeboy.ferratafit"
    compileSdk = 36

    defaultConfig {
        applicationId = "at.rudeboy.ferratafit"
        minSdk = 26
        targetSdk = 36
        versionCode = 12
        versionName = "1.11"
    }

    // Ein eigener Schluessel sorgt dafuer, dass spaetere Updates dieselbe Signatur
    // behalten und sich ueber die installierte App legen lassen.
    signingConfigs {
        if (hasSigningKey) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasSigningKey) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            if (hasSigningKey) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Bruecke zu Samsung Health: Samsung Health synchronisiert bidirektional mit Health Connect.
    implementation("androidx.health.connect:connect-client:1.1.0")

    testImplementation("junit:junit:4.13.2")
}
