import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
}


// firebase.properties מכיל רק את ארבעת ערכי הלקוח שסיפק המורה.
val firebaseProperties = Properties().apply {
    val localFile = rootProject.file("firebase.properties")
    localFile.inputStream().use(::load)
}

fun firebaseValue(name: String): String =
    firebaseProperties.getProperty(name)
        ?: error("Missing $name in firebase.properties")

android {
    namespace = "com.example.collectcircles"
    compileSdk {
        version = release(37)
    }

    buildFeatures {
        viewBinding = true
        resValues = true
    }


    defaultConfig {
        applicationId = "com.example.collectcircles"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                resValue("string", "google_app_id", firebaseValue("FIREBASE_APPLICATION_ID"))
                resValue("string", "google_api_key", firebaseValue("FIREBASE_API_KEY"))
                resValue("string", "project_id", firebaseValue("FIREBASE_PROJECT_ID"))
                resValue("string", "gcm_defaultSenderId", firebaseValue("FIREBASE_SENDER_ID"))
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.functions)
    androidTestImplementation(libs.ext.junit)
}