import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.signing)
}

android {
    namespace = "uk.co.tappable.feedback"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

val GROUP_ID = "uk.co.tappable"
val ARTIFACT_ID = "feedback"
val VERSION_NAME = "0.0.3" // Define your library's version
val localPropertiesFile = project.rootProject.file("local.properties")
val localProperties = Properties()
localProperties.load(localPropertiesFile.inputStream())

// Load credentials securely (from ~/.gradle/gradle.properties or environment variables)
val ossrhUsername =
    project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
val ossrhPassword =
    project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
val signingKeyId =
    project.findProperty("signing.keyId") as String? ?: System.getenv("SIGNING_KEY_ID")
val signingPassword =
    project.findProperty("signing.password") as String? ?: System.getenv("SIGNING_PASSWORD")
val signingKey = project.findProperty("signing.secretKeyRingFile") as String?
    ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")

// Load credentials securely
// For GitHub Packages, you'll typically use your GitHub username and a Personal Access Token (PAT)
val githubUser = localProperties["gpr.user"] as String?
    ?: System.getenv("GITHUB_ACTOR") // GITHUB_ACTOR is often set in GitHub Actions
val githubPassword = localProperties["gpr.key"] as String?
    ?: System.getenv("GITHUB_TOKEN") // GITHUB_TOKEN is often set in GitHub Actions

// ...

afterEvaluate { // Ensure Android components are created before configuring publishing
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = GROUP_ID
                artifactId = ARTIFACT_ID
                version = VERSION_NAME

                from(components["release"])

                pom {
                    name.set("Feedback Library")
                    description.set("A library to help providing feedback and debugging information")
                    url.set("https://github.com/The-App-Developers/feedback-tools") // Update with your actual repo URL

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("andrea-sciagura")
                            name.set("Andrea Sciagura")
                            email.set("andrea@tappable.co.uk")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/The-App-Developers/feedback-tools.git") // Update
//                        developerConnection.set("scm:git:ssh://github.com/yourusername/your-repo.git") // Update
                        url.set("https://github.com/The-App-Developers/feedback-tools/tree/main") // Update
                    }
                }
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                // Replace 'OWNER' with your GitHub username or organization
                // Replace 'REPOSITORY' with your GitHub repository name
                url = uri("https://maven.pkg.github.com/The-App-Developers/feedback-tools")
                credentials {
                    username = githubUser
                    password = githubPassword
                }
            }
        }
    }

    // Signing configuration remains the same if you still want to sign your artifacts
    if (project.hasProperty("signing.keyId") && signingKeyId != null && signingKey != null && signingPassword != null) {
        signing {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications)
        }
    }
}