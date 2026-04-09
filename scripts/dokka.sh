#!/usr/bin/env bash
#
# Generate HTML API documentation using Dokka CLI 2.2.0.
# Downloads Dokka and its dependencies on first run, caches them in .cache/.
#
# Usage:
#   ./scripts/dokka.sh              # generate docs for all library modules
#   ./scripts/dokka.sh --open       # generate and open in browser
#
# Output: build/dokka/html/

set -euo pipefail

DOKKA_VERSION="2.2.0"
KOTLINX_HTML_VERSION="0.8.0"
FREEMARKER_VERSION="2.3.31"

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
KSS_VERSION="$(grep -A2 'publishing:' "$PROJECT_DIR/kss.module-template.yaml" | grep 'version:' | sed 's/.*version: *"\(.*\)"/\1/')"
CACHE_DIR="$PROJECT_DIR/.cache/dokka"
OUTPUT_DIR="$PROJECT_DIR/build/dokka/html"

MAVEN_CENTRAL="https://repo1.maven.org/maven2"

# Dokka JARs
DOKKA_CLI_JAR="$CACHE_DIR/dokka-cli-$DOKKA_VERSION.jar"
DOKKA_BASE_JAR="$CACHE_DIR/dokka-base-$DOKKA_VERSION.jar"
ANALYSIS_JAR="$CACHE_DIR/analysis-kotlin-descriptors-$DOKKA_VERSION.jar"
KOTLINX_HTML_JAR="$CACHE_DIR/kotlinx-html-jvm-$KOTLINX_HTML_VERSION.jar"
FREEMARKER_JAR="$CACHE_DIR/freemarker-$FREEMARKER_VERSION.jar"

mkdir -p "$CACHE_DIR"

download_jar() {
    local url="$1"
    local dest="$2"
    local name
    name="$(basename "$dest")"
    if [ ! -f "$dest" ]; then
        echo "Downloading $name..."
        curl -sSfL "$url" -o "$dest"
    fi
}

download_deps() {
    download_jar \
        "$MAVEN_CENTRAL/org/jetbrains/dokka/dokka-cli/$DOKKA_VERSION/dokka-cli-$DOKKA_VERSION.jar" \
        "$DOKKA_CLI_JAR"

    download_jar \
        "$MAVEN_CENTRAL/org/jetbrains/dokka/dokka-base/$DOKKA_VERSION/dokka-base-$DOKKA_VERSION.jar" \
        "$DOKKA_BASE_JAR"

    download_jar \
        "$MAVEN_CENTRAL/org/jetbrains/dokka/analysis-kotlin-descriptors/$DOKKA_VERSION/analysis-kotlin-descriptors-$DOKKA_VERSION.jar" \
        "$ANALYSIS_JAR"

    download_jar \
        "$MAVEN_CENTRAL/org/jetbrains/kotlinx/kotlinx-html-jvm/$KOTLINX_HTML_VERSION/kotlinx-html-jvm-$KOTLINX_HTML_VERSION.jar" \
        "$KOTLINX_HTML_JAR"

    download_jar \
        "$MAVEN_CENTRAL/org/freemarker/freemarker/$FREEMARKER_VERSION/freemarker-$FREEMARKER_VERSION.jar" \
        "$FREEMARKER_JAR"
}

generate_config() {
    cat > "$CACHE_DIR/dokka-config.json" <<JSONEOF
{
  "moduleName": "kss",
  "moduleVersion": "$KSS_VERSION",
  "outputDir": "$OUTPUT_DIR",
  "failOnWarning": false,
  "suppressObviousFunctions": true,
  "suppressInheritedMembers": false,
  "offlineMode": false,
  "sourceLinks": [
    {
      "localDirectory": "$PROJECT_DIR",
      "remoteUrl": "https://github.com/dev-tonholo/kss/tree/main",
      "remoteLineSuffix": "#L"
    }
  ],
  "externalDocumentationLinks": [
    {
      "url": "https://kotlinlang.org/api/core/kotlin-stdlib/",
      "packageListUrl": "https://kotlinlang.org/api/core/kotlin-stdlib/package-list"
    }
  ],
  "perPackageOptions": [
    {
      "matchingRegex": ".*internal.*",
      "suppress": true,
      "documentedVisibilities": ["PUBLIC"],
      "reportUndocumented": false,
      "skipDeprecated": false
    }
  ],
  "sourceSets": [
    {
      "displayName": "core",
      "sourceSetID": {
        "scopeId": "kss",
        "sourceSetName": "core"
      },
      "documentedVisibilities": ["PUBLIC", "PROTECTED"],
      "reportUndocumented": false,
      "skipEmptyPackages": true,
      "skipDeprecated": false,
      "jdkVersion": 21,
      "languageVersion": "2.3",
      "apiVersion": "2.3",
      "noStdlibLink": false,
      "noJdkLink": false,
      "analysisPlatform": "common",
      "sourceRoots": ["$PROJECT_DIR/core/src"]
    },
    {
      "displayName": "lexer",
      "sourceSetID": {
        "scopeId": "kss",
        "sourceSetName": "lexer"
      },
      "documentedVisibilities": ["PUBLIC", "PROTECTED"],
      "reportUndocumented": false,
      "skipEmptyPackages": true,
      "skipDeprecated": false,
      "jdkVersion": 21,
      "languageVersion": "2.3",
      "apiVersion": "2.3",
      "noStdlibLink": false,
      "noJdkLink": false,
      "analysisPlatform": "common",
      "sourceRoots": ["$PROJECT_DIR/lexer/src"]
    },
    {
      "displayName": "parser",
      "sourceSetID": {
        "scopeId": "kss",
        "sourceSetName": "parser"
      },
      "documentedVisibilities": ["PUBLIC", "PROTECTED"],
      "reportUndocumented": false,
      "skipEmptyPackages": true,
      "skipDeprecated": false,
      "jdkVersion": 21,
      "languageVersion": "2.3",
      "apiVersion": "2.3",
      "noStdlibLink": false,
      "noJdkLink": false,
      "analysisPlatform": "common",
      "sourceRoots": ["$PROJECT_DIR/parser/src"]
    }
  ],
  "pluginsClasspath": [
    "$DOKKA_BASE_JAR",
    "$ANALYSIS_JAR",
    "$KOTLINX_HTML_JAR",
    "$FREEMARKER_JAR"
  ]
}
JSONEOF
}

echo "=== Dokka $DOKKA_VERSION API Documentation Generator ==="
echo ""

download_deps

echo ""
echo "Generating documentation..."
generate_config

java -jar "$DOKKA_CLI_JAR" "$CACHE_DIR/dokka-config.json"

HTML_COUNT=$(find "$OUTPUT_DIR" -name "*.html" 2>/dev/null | wc -l)
echo ""
echo "Documentation generated: $OUTPUT_DIR"
echo "HTML files: $HTML_COUNT"

if [ "${1:-}" = "--open" ]; then
    INDEX="$OUTPUT_DIR/index.html"
    if [ -f "$INDEX" ]; then
        if command -v xdg-open &>/dev/null; then
            xdg-open "$INDEX"
        elif command -v open &>/dev/null; then
            open "$INDEX"
        else
            echo "Open manually: $INDEX"
        fi
    fi
fi
