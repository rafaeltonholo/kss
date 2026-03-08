#!/usr/bin/env bash
#
# Code quality checks: ktlint (format) + detekt (static analysis).
# Downloads tools on first run and caches them in .cache/.
#
# Usage:
#   ./scripts/check.sh              # run both ktlint check + detekt
#   ./scripts/check.sh format       # auto-format with ktlint
#   ./scripts/check.sh ktlint       # run ktlint check only
#   ./scripts/check.sh detekt       # run detekt only

set -euo pipefail

KTLINT_VERSION="1.8.0"
DETEKT_VERSION="2.0.0-alpha.2"
COMPOSE_RULES_VERSION="0.5.6"

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CACHE_DIR="$PROJECT_DIR/.cache"
KTLINT_BIN="$CACHE_DIR/ktlint-$KTLINT_VERSION"
DETEKT_ZIP="$CACHE_DIR/detekt-cli-$DETEKT_VERSION.zip"
DETEKT_DIR="$CACHE_DIR/detekt-cli-$DETEKT_VERSION"
DETEKT_BIN="$DETEKT_DIR/bin/detekt-cli"
COMPOSE_RULES_KTLINT_JAR="$CACHE_DIR/ktlint-compose-$COMPOSE_RULES_VERSION-all.jar"
COMPOSE_RULES_DETEKT_JAR="$CACHE_DIR/detekt-compose-$COMPOSE_RULES_VERSION-all.jar"

mkdir -p "$CACHE_DIR"

download_ktlint() {
    if [ ! -f "$KTLINT_BIN" ]; then
        echo "Downloading ktlint $KTLINT_VERSION..."
        curl -sSL \
            "https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint" \
            -o "$KTLINT_BIN"
        chmod +x "$KTLINT_BIN"
    fi
}

download_detekt() {
    if [ ! -f "$DETEKT_BIN" ]; then
        echo "Downloading detekt $DETEKT_VERSION..."
        curl -sSL \
            "https://github.com/detekt/detekt/releases/download/v$DETEKT_VERSION/detekt-cli-$DETEKT_VERSION.zip" \
            -o "$DETEKT_ZIP"
        unzip -qo "$DETEKT_ZIP" -d "$CACHE_DIR"
        rm -f "$DETEKT_ZIP"
        chmod +x "$DETEKT_BIN"
    fi
}

download_compose_rules() {
    if [ ! -f "$COMPOSE_RULES_KTLINT_JAR" ]; then
        echo "Downloading compose-rules ktlint $COMPOSE_RULES_VERSION..."
        curl -sSL \
            "https://github.com/mrmans0n/compose-rules/releases/download/v$COMPOSE_RULES_VERSION/ktlint-compose-$COMPOSE_RULES_VERSION-all.jar" \
            -o "$COMPOSE_RULES_KTLINT_JAR"
    fi
    if [ ! -f "$COMPOSE_RULES_DETEKT_JAR" ]; then
        echo "Downloading compose-rules detekt $COMPOSE_RULES_VERSION..."
        curl -sSL \
            "https://github.com/mrmans0n/compose-rules/releases/download/v$COMPOSE_RULES_VERSION/detekt-compose-$COMPOSE_RULES_VERSION-all.jar" \
            -o "$COMPOSE_RULES_DETEKT_JAR"
    fi
}

run_ktlint_check() {
    download_ktlint
    download_compose_rules
    echo "Running ktlint..."
    "$KTLINT_BIN" \
        -R "$COMPOSE_RULES_KTLINT_JAR" \
        --editorconfig="$PROJECT_DIR/.editorconfig" \
        "$PROJECT_DIR/kss-*/src/**/*.kt" \
        "$PROJECT_DIR/kss-*/test/**/*.kt"
}

run_ktlint_format() {
    download_ktlint
    download_compose_rules
    echo "Running ktlint format..."
    "$KTLINT_BIN" \
        --format \
        -R "$COMPOSE_RULES_KTLINT_JAR" \
        --editorconfig="$PROJECT_DIR/.editorconfig" \
        "$PROJECT_DIR/kss-*/src/**/*.kt" \
        "$PROJECT_DIR/kss-*/test/**/*.kt"
}

run_detekt() {
    download_detekt
    download_compose_rules
    echo "Running detekt..."
    "$DETEKT_BIN" \
        --input "$PROJECT_DIR/kss-core/src,$PROJECT_DIR/kss-lexer/src,$PROJECT_DIR/kss-parser/src,$PROJECT_DIR/kss-demo/src,$PROJECT_DIR/kss-demo-desktop/src,$PROJECT_DIR/kss-demo-web/src,$PROJECT_DIR/kss-lexer/test,$PROJECT_DIR/kss-parser/test" \
        --config "$PROJECT_DIR/detekt.yml" \
        --plugins "$COMPOSE_RULES_DETEKT_JAR" \
        --build-upon-default-config \
        --baseline "$PROJECT_DIR/detekt-baseline.xml"
}

case "${1:-all}" in
    format)
        run_ktlint_format
        ;;
    ktlint)
        run_ktlint_check
        ;;
    detekt)
        run_detekt
        ;;
    all)
        run_ktlint_check
        echo ""
        run_detekt
        ;;
    *)
        echo "Usage: $0 [format|ktlint|detekt|all]"
        exit 1
        ;;
esac

echo ""
echo "All checks passed."
