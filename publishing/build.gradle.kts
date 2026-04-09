plugins {
    kotlin("multiplatform") version "2.3.20" apply false
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
}

// Read group and version from the shared Amper template (single source of truth).
val templateFile = rootProject.file("../kss.module-template.yaml")
val publishingMatch = Regex("""publishing:\s*\n\s+group:\s*"(.+?)"\s*\n\s+version:\s*"(.+?)"""")
    .find(templateFile.readText())
    ?: error("Could not read publishing settings from kss.module-template.yaml")
val kssGroup = publishingMatch.groupValues[1]
val kssVersion = publishingMatch.groupValues[2]

subprojects {
    group = kssGroup
    version = kssVersion
}

subprojects {
    afterEvaluate {
        extensions.findByType<com.vanniktech.maven.publish.MavenPublishBaseExtension>()?.apply {
            publishToMavenCentral()

            val signingKey = findProperty("signingInMemoryKey") as String?
                ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")
            if (!signingKey.isNullOrBlank()) {
                signAllPublications()
            }

            pom {
                url.set("https://github.com/dev-tonholo/kss")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("rafaeltonholo")
                        name.set("Rafael Tonholo")
                    }
                }
                organization {
                    name.set("dev-tonholo")
                    url.set("https://github.com/dev-tonholo")
                }
                scm {
                    connection.set("scm:git:git://github.com/dev-tonholo/kss.git")
                    developerConnection.set("scm:git:ssh://github.com:dev-tonholo/kss.git")
                    url.set("https://github.com/dev-tonholo/kss")
                }
            }
        }

        // GitHub Packages remains a manual publishing repository.
        extensions.findByType<PublishingExtension>()?.apply {
            repositories {
                maven {
                    name = "githubPackages"
                    url = uri("https://maven.pkg.github.com/dev-tonholo/kss")
                    credentials {
                        username = findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}
