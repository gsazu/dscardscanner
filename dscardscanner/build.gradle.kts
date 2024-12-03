plugins {
    id("com.android.library")
    id("maven-publish")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ds.cardscanner"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
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

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Google ML Kit Object Detection
    implementation("com.google.mlkit:object-detection:17.0.2")

    // Google ML Kit Text Recognition (Latin Script)
    implementation("com.google.mlkit:text-recognition:16.0.1")
}

afterEvaluate {
    publishing {
        publications {
            android.libraryVariants.forEach { variant ->
                create<MavenPublication>(variant.name) {
                    from(components[variant.name])

                    groupId = "com.ds.cardscanner"
                    artifactId = "dsCardScanner"
                    version = "1.0.0"
                }
            }
        }
    }
}
