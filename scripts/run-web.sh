#!/usr/bin/env bash
#
# Build the kss-demo-web module and serve it locally with a dev server.
# Requires Node.js (>= 18) and npm.
#
# Usage:
#   ./scripts/run-web.sh                # build + serve on http://localhost:3000
#   ./scripts/run-web.sh --skip-build   # serve without rebuilding

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_OUTPUT="$PROJECT_DIR/build/tasks/_kss-demo-web_linkWasmJs"
SERVE_DIR="$PROJECT_DIR/build/web-dev"
AMPER_CACHE="$HOME/Library/Caches/JetBrains/Amper/.m2.cache"

# --- Build ---
if [ "${1:-}" != "--skip-build" ]; then
    echo "Building kss-demo-web..."
    "$PROJECT_DIR/amper" build -m kss-demo-web
fi

if [ ! -f "$BUILD_OUTPUT/kss-demo-web.wasm" ]; then
    echo "Error: Build output not found at $BUILD_OUTPUT"
    echo "Run without --skip-build first."
    exit 1
fi

# --- Find skiko runtime jar ---
SKIKO_JAR=$(find "$AMPER_CACHE" -path "*/skiko-js-wasm-runtime/*/skiko-js-wasm-runtime-*.jar" \
    ! -name "*-sources.jar" 2>/dev/null | sort -V | tail -1)

if [ -z "$SKIKO_JAR" ]; then
    echo "Error: skiko-js-wasm-runtime jar not found in Amper cache."
    echo "Run a full build first: ./amper build -m kss-demo-web"
    exit 1
fi

echo "Using skiko runtime: $SKIKO_JAR"

# --- Prepare serve directory ---
rm -rf "$SERVE_DIR"
mkdir -p "$SERVE_DIR"

# Copy Kotlin/WASM build artifacts
cp "$BUILD_OUTPUT"/kss-demo-web.mjs "$SERVE_DIR/"
cp "$BUILD_OUTPUT"/kss-demo-web.uninstantiated.mjs "$SERVE_DIR/"
cp "$BUILD_OUTPUT"/kss-demo-web.wasm "$SERVE_DIR/"

# Extract skiko runtime (skiko.mjs, skiko.wasm)
unzip -qo "$SKIKO_JAR" skiko.mjs skiko.wasm -d "$SERVE_DIR/"

# Copy index.html
cp "$PROJECT_DIR/kss-demo-web/index.html" "$SERVE_DIR/"

# Create a minimal package.json for the dev server
cat > "$SERVE_DIR/package.json" <<'PACKAGE_EOF'
{
  "name": "kss-demo-web-dev",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite --port 3000"
  },
  "dependencies": {
    "@js-joda/core": "^5.6.3"
  },
  "devDependencies": {
    "vite": "^6.0.0"
  }
}
PACKAGE_EOF

# Vite config: COOP/COEP headers required for SharedArrayBuffer (used by skiko)
cat > "$SERVE_DIR/vite.config.js" <<'VITE_EOF'
import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    headers: {
      'Cross-Origin-Opener-Policy': 'same-origin',
      'Cross-Origin-Embedder-Policy': 'require-corp',
    },
  },
});
VITE_EOF

# --- Install dependencies and serve ---
echo "Installing npm dependencies..."
cd "$SERVE_DIR"
npm install --silent 2>&1 | tail -3

echo ""
echo "Starting dev server at http://localhost:3000"
echo ""
npx vite --port 3000
