# Contributing to KSS

Thank you for your interest in contributing to KSS! This document provides guidelines and instructions for contributing.

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected
to uphold this code. Please report unacceptable behavior by opening an issue.

## How to Contribute

### Reporting Bugs

Before creating a bug report, please check [existing issues](https://github.com/dev-tonholo/kss/issues) to avoid
duplicates.

When filing a bug report, include:

- A clear, descriptive title
- Steps to reproduce the issue
- Expected vs. actual behavior
- The CSS input that triggers the bug (if applicable)
- Your environment (OS, JDK version, Kotlin version)

### Suggesting Enhancements

Enhancement suggestions are tracked as [GitHub issues](https://github.com/dev-tonholo/kss/issues). When creating an
enhancement suggestion, include:

- A clear, descriptive title
- A detailed description of the proposed enhancement
- An explanation of why this would be useful

### Pull Requests

1. **Fork** the repository and create your branch from `main`.
2. **Follow the code style** enforced by ktlint and detekt (see below).
3. **Add tests** for any new functionality.
4. **Run the checks** before submitting:
   ```bash
   ./amper test
   ./scripts/check.sh
   ```
5. **Write a clear PR description** explaining what your changes do and why.

## Development Setup

### Prerequisites

- **JDK 17+**
- A Kotlin-aware IDE (IntelliJ IDEA recommended)

### Building

```bash
# Build all modules
./amper build

# Run tests
./amper test

# Run a specific module's tests
./amper test -m kss-lexer
```

### Code Quality

The project uses [ktlint](https://github.com/pinterest/ktlint) for formatting
and [detekt](https://github.com/detekt/detekt) for static analysis,
with [compose-rules](https://github.com/mrmans0n/compose-rules) for Compose-specific linting.

```bash
# Run all checks
./scripts/check.sh

# Auto-format code
./scripts/check.sh format

# Run only ktlint or detekt
./scripts/check.sh ktlint
./scripts/check.sh detekt
```

Tools are downloaded and cached automatically on first run.

### Code Style

- Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Maximum line length: **120 characters**
- Use the project `.editorconfig` for IDE formatting
- Do not suppress lint warnings — fix them instead

## Commit Messages

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `build`, `chore`

**Scopes**: `kss-core`, `kss-lexer`, `kss-parser`, `kss-demo`

Examples:

- `feat(kss-lexer): add support for @layer at-rule tokenization`
- `fix(kss-parser): handle empty declaration blocks without crashing`
- `docs: update README with new API examples`

## Project Structure

```
kss-core/       Core abstractions (Token, CssLocation, AST base types)
kss-lexer/      CSS tokenizer
kss-parser/     CSS parser (tokens -> AST)
kss-demo/       Shared Compose UI for the demo app
kss-demo-desktop/   Desktop entry point
kss-demo-web/       Web (wasmJs) entry point
scripts/        Build and quality scripts
```

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
