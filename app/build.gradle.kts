plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hepta.lsplantdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hepta.lsplantdemo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {

            // For ndk-build, instead use the ndkBuild block.
            cmake {
                // Sets optional flags for the C++ compiler.
                cppFlags += listOf("-std=c++20")
            }
        }



        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a","x86","x86_64")
        }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
        prefab = true

    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.dobby)
    implementation(libs.lsplant)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}