plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cl.umayor.lablight"
    compileSdk = 34

    defaultConfig {
        applicationId = "cl.umayor.lablight"
        minSdk = 26          // Android 8.0 o superior
        targetSdk = 34
        versionCode = 2
        versionName = "0.2.0"   // Gate 4 · MVP integrado
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        // Permite acceder a los elementos de cada pantalla sin findViewById
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Gráficos (decisión de diseño congelada: MPAndroidChart)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Pruebas automáticas de los cálculos (Analysis.kt)
    testImplementation("junit:junit:4.13.2")
}
