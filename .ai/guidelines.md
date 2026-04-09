# KSS — AI Agent Guidelines

These guidelines MUST be followed at all times when working in this repository.

## Repository Overview

KSS (Kotlin Style Sheets) is a Kotlin Multiplatform CSS lexer and parser
library. It provides:

1. **A CSS tokenizer** (`lexer`) — converts CSS text into a stream of tokens
   following the CSS Syntax Module Level 3 specification.
2. **A CSS parser** (`parser`) — converts tokens into a `StyleSheet` AST
   (Abstract Syntax Tree).
3. **Shared abstractions** (`core`) — `Token`, `TokenKind`, `Element`, and AST
   base types used across modules.

**Current version**: `1.0.1-SNAPSHOT` (defined in `publishing/build.gradle.kts`).

## Project Structure

```
.
├── AGENTS.md                          # Root repository agent context
├── .ai/
│   ├── guidelines.md                  # AI agent guidelines (you are here)
│   └── skills/                        # Reusable AI skill definitions
├── core/                              # Shared abstractions (Token, AST base)
├── lexer/                             # CSS tokenizer
├── parser/                            # CSS parser (AST builder)
├── demo/
│   ├── shared/                        # Shared Compose Multiplatform UI
│   ├── desktop/                       # Desktop entry point (JVM)
│   └── web/                           # Web entry point (wasmJs)
├── publishing/                        # Gradle-based Maven publication
├── scripts/                           # Build helper scripts
│   ├── check.sh                       # ktlint + detekt runner
│   ├── dokka.sh                       # API documentation generator
│   └── run-web.sh                     # Web demo launcher
├── detekt.yml                         # Detekt static analysis configuration
├── .editorconfig                      # Code style settings
├── kss.module-template.yaml           # Amper shared module template
├── project.yaml                       # Amper project definition
└── libs.versions.toml                 # Dependency version catalog
```

## Build Commands

### Building

```bash
./amper build                          # Build everything
./amper build -m core                  # Build core module only
./amper build -m lexer                 # Build lexer module only
./amper build -m parser                # Build parser module only
```

### Testing

```bash
./amper test                           # Run all tests
./amper test -m lexer                  # Test lexer module only
./amper test -m parser                 # Test parser module only
./amper test -m demo/shared            # Test demo shared module
```

### Static Analysis

```bash
./scripts/check.sh                     # Run ktlint + detekt
./scripts/check.sh detekt              # Run detekt only
./scripts/check.sh ktlint              # Run ktlint check only
./scripts/check.sh format              # Auto-format with ktlint
```

### Publishing

```bash
cd publishing && ./gradlew publishAllPublicationsToMavenLocalRepository
```

### Documentation

```bash
./scripts/dokka.sh                     # Generate API docs
```

### Demo

```bash
./scripts/run-web.sh                   # Run the web demo (wasmJs)
```

## Code Style and Quality

- **Kotlin code style**: Official (`ij_kotlin_code_style_defaults = KOTLIN_OFFICIAL`
  in `.editorconfig`).
- **Static analysis**: Detekt with Compose rules + ktlint. Configuration in
  `detekt.yml` and `.editorconfig`.
- **Max line length**: 120 characters.
- **Both ktlint and Detekt must pass** before any PR can be merged. Run
  `./scripts/check.sh` locally.
- **Tests must pass**: Run `./amper test` to verify across all targets.

### Key Detekt Rules

- Cyclomatic complexity threshold: 15
- Max method length: 60 lines
- Max function parameters: 6 (ignoring defaults)
- Max functions per file/class: 15
- Max return statements: 3
- `TODO`/`FIXME`/`STOPSHIP` comments must link to a GitHub issue
- Compose rules are active (modifier ordering, naming, etc.)

## Commit Conventions

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>
```

**Types**: `feat`, `fix`, `docs`, `refactor`, `perf`, `test`, `chore`

**Scopes** (optional): `core`, `lexer`, `parser`, `demo`, `build`, `ci`,
`publishing`, etc.

**Rules**:

- Subject under ~72 characters, imperative mood.
- Reference GitHub issues when applicable (`Closes #123`).
- Keep non-functional changes in separate commits.

**Examples**:

- `feat(lexer): add support for CSS custom property tokens`
- `fix(parser): handle malformed at-rule preludes`
- `chore(build): update Kotlin to 2.3.20`

## Branch Naming

```
feat/<short-topic>
fix/<short-topic>
docs/<short-topic>
chore/<short-topic>
```

## Testing Guidelines

- **Framework**: `kotlin.test` (common source set via Amper `$kotlin.test`).
- **Power Assert**: Use for rich failure messages where available.
- **Burst**: Use for parameterized tests.
- Tests live in `test/` directories within each module and mirror the source
  package structure.
- Add or update tests for any behavioral change.

## Module-Specific Guidelines

Before modifying code in a module, read its `AGENTS.md`:

| Module         | Docs                                                |
|----------------|-----------------------------------------------------|
| `core`         | [core/AGENTS.md](../core/AGENTS.md)                 |
| `lexer`        | [lexer/AGENTS.md](../lexer/AGENTS.md)               |
| `parser`       | [parser/AGENTS.md](../parser/AGENTS.md)             |
| `demo/shared`  | [demo/shared/AGENTS.md](../demo/shared/AGENTS.md)   |
| `demo/desktop` | [demo/desktop/AGENTS.md](../demo/desktop/AGENTS.md) |
| `demo/web`     | [demo/web/AGENTS.md](../demo/web/AGENTS.md)         |

## Key Architectural Decisions

- **Kotlin Multiplatform**: Targets jvm, macosArm64, macosX64, linuxX64, js, and
  wasmJs. Core logic stays in common source sets.
- **Amper Build System**: Module configuration in `module.yaml` files. Shared
  settings in `kss.module-template.yaml`. Do not duplicate configuration across
  modules.
- **Version Catalog**: Dependency versions are managed in `libs.versions.toml`.
- **Module exports**: `lexer` exports `core`, `parser` exports both `core` and
  `lexer`. Consumers only need to depend on `parser` to get the full stack.

## Multiplatform Constraints

- **Source Set Isolation**: Core logic MUST stay in common source sets. Do not
  introduce JVM-specific dependencies (like `java.io`, `java.nio`, or `javax.*`)
  into common code.
- **Platform-Specific Code**: Use Amper's `src@<platform>/` directories (e.g.,
  `src@jvm/`, `src@wasmJs/`) for platform-specific implementations.
- **Dependency Compatibility**: Before adding a library, verify it supports all
  current targets: `jvm`, `macosArm64`, `macosX64`, `linuxX64`, `js`, `wasmJs`.

## CI/CD

- **PR checks** (`.github/workflows/ci.yml`): Builds, runs static analysis, and
  runs all tests.
- **Publishing** (`.github/workflows/publish.yml`): Publishes to Maven Central
  and GitHub Packages.
- All CI checks must pass before merging.

## Prerequisites

- JDK 17+ (for Amper)
- Git
- IDE with Kotlin support (IntelliJ IDEA or Fleet recommended)
