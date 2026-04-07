# KSS Publishing Wrapper

## Why this directory exists

KSS uses [JetBrains Amper](https://github.com/JetBrains/amper) as its primary build
system. However, Amper currently only supports publishing JVM artifacts. Since KSS is a
Kotlin Multiplatform library targeting JVM, JS, wasmJs, linuxX64, macosArm64, and macosX64,
we need a Gradle project that can compile from the same sources and publish proper KMP
artifacts with Gradle Module Metadata.

## How it works

This Gradle project does **not** duplicate any source code. Each module's `build.gradle.kts`
points back to the original source directories via `kotlin.srcDir(...)` references:

```
publishing/core/build.gradle.kts  -->  ../../core/src
publishing/lexer/build.gradle.kts -->  ../../lexer/src
publishing/parser/build.gradle.kts -> ../../parser/src
```

## Usage

### Local publishing (development)

```bash
cd publishing
./gradlew publishToMavenLocal
```

Artifacts are written to `~/.m2/repository/dev/tonholo/kss/`.

### CI releases

The CI workflow invokes this Gradle project to publish to Maven Central and GitHub Packages.
Credentials are provided via environment variables:

| Variable                                      | Purpose                         |
|-----------------------------------------------|---------------------------------|
| `ORG_GRADLE_PROJECT_mavenCentralUsername`      | Maven Central (Central Portal)  |
| `ORG_GRADLE_PROJECT_mavenCentralPassword`      | Maven Central (Central Portal)  |
| `GITHUB_ACTOR`                                 | GitHub Packages                 |
| `GITHUB_TOKEN`                                 | GitHub Packages                 |
| `ORG_GRADLE_PROJECT_signingInMemoryKey`        | Artifact signing (GPG key)      |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyId`      | Artifact signing (GPG key ID)   |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword`| Artifact signing (GPG password) |

### Published artifacts

| Artifact ID  | Description             |
|--------------|-------------------------|
| `kss-core`   | Core abstractions       |
| `kss-lexer`  | CSS tokenizer           |
| `kss-parser` | CSS parser              |
| `kss-bom`    | Bill of Materials (BOM) |

Each library module publishes platform-specific artifacts automatically (e.g.,
`kss-core-jvm`, `kss-core-js`, `kss-core-wasmjs`, etc.).

## Important notes

- **Amper remains the primary build tool** for day-to-day development, running tests, and
  building the demo applications.
- This Gradle wrapper is only used for **publishing** KMP artifacts.
- **When can this be removed?** Once Amper adds native KMP publishing support, this wrapper
  will no longer be necessary and should be deleted.
