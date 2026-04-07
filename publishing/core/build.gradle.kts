plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    linuxX64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain {
            kotlin.srcDir("../../core/src")
        }
    }

}

mavenPublishing {
    coordinates(artifactId = "kss-core")
    pom {
        name.set("KSS Core")
        description.set("Core abstractions for KSS - Kotlin Style Sheets")
    }
}
