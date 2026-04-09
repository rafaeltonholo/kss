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
            kotlin.srcDir("../../lexer/src")
            dependencies {
                api(project(":core"))
            }
        }
        commonTest {
            kotlin.srcDir("../../lexer/test")
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

}

mavenPublishing {
    coordinates(artifactId = "kss-lexer")
    pom {
        name.set("KSS Lexer")
        description.set("CSS tokenizer for KSS - Kotlin Style Sheets")
    }
}
