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
    mingwX64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain {
            kotlin.srcDir("../../parser/src")
            dependencies {
                api(project(":core"))
                api(project(":lexer"))
            }
        }
        commonTest {
            kotlin.srcDir("../../parser/test")
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

}

mavenPublishing {
    coordinates(artifactId = "kss-parser")
    pom {
        name.set("KSS Parser")
        description.set("CSS parser for KSS - Kotlin Style Sheets")
    }
}
