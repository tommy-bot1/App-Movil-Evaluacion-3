import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tacticaldev.trazabilidad"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tacticaldev.trazabilidad"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URLs de los microservicios. En el emulador, 10.0.2.2 apunta al PC donde corre Spring Boot.
        // En un celular físico: ./gradlew assembleDebug -PapiHost=http://192.168.1.50  (IP del PC en la misma red)
        val apiHost = (project.findProperty("apiHost") as String?) ?: "http://10.0.2.2"
        buildConfigField("String", "URL_USUARIOS", "\"$apiHost:8081/\"")
        buildConfigField("String", "URL_VISITAS", "\"$apiHost:8082/\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            // Activa el reporte de cobertura de las pruebas unitarias (tarea createDebugUnitTestCoverageReport)
            enableUnitTestCoverage = true
        }
    }

    testOptions {
        unitTests {
            // Las clases de Android sin implementar devuelven valores por defecto en vez de lanzar error
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    // Pruebas unitarias: dobles de prueba (MockK) y corrutinas controlables (coroutines-test)
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.9.0"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navegación y ViewModel con Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Íconos extendidos de Material Design
    implementation("androidx.compose.material:material-icons-extended")

    // Persistencia Local: Room Database (Procesado con KSP 2.7.1)
    val versionRoom = "2.7.1"
    implementation("androidx.room:room-runtime:$versionRoom")
    implementation("androidx.room:room-ktx:$versionRoom")
    ksp("androidx.room:room-compiler:$versionRoom")

    // Recursos Nativos: Geolocalización GPS
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Consumo de microservicios y APIs REST: Retrofit + Gson + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Carga e inspección de imágenes de cámara (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")
}