# KSS — Kotlin Style Sheets

[![Build](https://github.com/dev-tonholo/kss/actions/workflows/ci.yml/badge.svg)](https://github.com/dev-tonholo/kss/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/dev.tonholo.kss/kss-core)](https://central.sonatype.com/namespace/dev.tonholo.kss)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue.svg?logo=kotlin)](https://kotlinlang.org)

A **Kotlin Multiplatform** CSS lexer and parser that produces a fully navigable Abstract Syntax Tree (AST) with
source-position tracking.

KSS tokenizes and parses CSS following the [W3C CSS Syntax Level 3](https://www.w3.org/TR/css-syntax-3/) specification,
providing precise character offsets for every token and AST node. It runs on JVM, macOS, Linux, JS, and WebAssembly.

## Modules

| Module           | Description                                                                   |
|------------------|-------------------------------------------------------------------------------|
| **core**         | Shared abstractions: `Token`, `TokenKind`, `CssLocation`, AST node base types |
| **lexer**        | CSS tokenizer — turns a CSS string into `List<Token<out CssTokenKind>>`       |
| **parser**       | CSS parser — turns tokens into a `StyleSheet` AST                             |
| **bom**          | Bill of Materials for version alignment                                       |
| **demo/shared**  | Shared Compose Multiplatform UI for the demo app                              |
| **demo/desktop** | Desktop (JVM) entry point for the demo                                        |
| **demo/web**     | Web (wasmJs) entry point for the demo                                         |

## Installation

### Kotlin Multiplatform (Gradle Kotlin DSL)

```kotlin
// Use the BOM for version alignment
dependencies {
    implementation(platform("dev.tonholo.kss:kss-bom:1.0.1-SNAPSHOT"))
    implementation("dev.tonholo.kss:kss-parser")  // includes core + lexer transitively
}
```

### Kotlin JVM (Gradle Kotlin DSL)

```kotlin
dependencies {
    implementation(platform("dev.tonholo.kss:kss-bom:1.0.1-SNAPSHOT"))
    implementation("dev.tonholo.kss:kss-parser")
}
```

### Amper

In your `module.yaml`:

```yaml
dependencies:
    - dev.tonholo.kss:kss-parser:1.0.1-SNAPSHOT
```

Or with individual modules:

```yaml
dependencies:
    - dev.tonholo.kss:kss-core:1.0.1-SNAPSHOT
    - dev.tonholo.kss:kss-lexer:1.0.1-SNAPSHOT
```

## Quick Start

### Tokenize CSS

```kotlin
import dev.tonholo.kss.lexer.css.CssTokenizer

val css = "body { color: red; }"
val tokens = CssTokenizer().tokenize(css)

for (token in tokens) {
    println("${token.kind} [${token.startOffset}..${token.endOffset}]")
}
```

### Parse CSS to AST

```kotlin
import dev.tonholo.kss.lexer.css.CssTokenizer
import dev.tonholo.kss.parser.ast.css.CssParser
import dev.tonholo.kss.parser.ast.css.consumer.CssConsumers

val css = "body { color: red; }"
val tokens = CssTokenizer().tokenize(css)
val consumers = CssConsumers(css)
val styleSheet = CssParser(consumers).parse(tokens)

// Navigate the AST
for (child in styleSheet.children) {
    println(child)
}
```

## Demo App

The project includes a Compose Multiplatform demo app — a CSS AST Explorer with a split-pane layout: a
syntax-highlighted CSS editor on the left and an interactive AST tree viewer on the right, with bidirectional
synchronization.

### Run Desktop

```bash
./amper run -m demo/desktop
```

### Run Web

Requires [Node.js](https://nodejs.org/) (>= 18) and npm:

```bash
./scripts/run-web.sh
```

This builds the wasmJs module, extracts the Skiko runtime, installs npm dependencies, and starts
a Vite dev server at `http://localhost:3000`.

## Building

KSS uses the [Amper](https://github.com/JetBrains/amper) build system.

```bash
# Build all modules
./amper build

# Run tests
./amper test

# Run code quality checks (ktlint + detekt)
./scripts/check.sh

# Auto-format with ktlint
./scripts/check.sh format
```

## Requirements

- **JDK 17+** (for building)
- **Node.js 18+** and **npm** (for the web demo only)
- **Amper** is bootstrapped automatically via the included wrapper scripts

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute.

## License

This project is licensed under the [MIT License](LICENSE).
