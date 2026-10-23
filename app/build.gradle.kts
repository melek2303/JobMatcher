plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.projetjobmatcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projetjobmatcher"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    // ... dependances existantes ...
    // iText7 : librairie pour lire et extraire le texte des fichiers PDF
    // Sans cette librairie, Android ne peut pas lire le contenu d'un PDF
    implementation(libs.itext7.core)
    // Bouncy Castle : librairie de securite requise par iText7
    // Elle gere le dechiffrement des PDF proteges par mot de passe
    implementation(libs.bouncy.castle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
