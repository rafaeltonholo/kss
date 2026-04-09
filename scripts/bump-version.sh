#!/usr/bin/env bash
#
# Bump the KSS library version everywhere.
#
# The single source of truth is kss.module-template.yaml. This script updates
# that file plus all documentation references that embed the version string.
#
# Usage:
#   ./scripts/bump-version.sh <new-version>
#   ./scripts/bump-version.sh 1.1.0
#   ./scripts/bump-version.sh 1.1.0-SNAPSHOT

set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <new-version>"
    echo "Example: $0 1.1.0"
    exit 1
fi

NEW_VERSION="$1"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TEMPLATE="$PROJECT_DIR/kss.module-template.yaml"

# Read current version from the template (single source of truth).
CURRENT_VERSION="$(grep -A2 'publishing:' "$TEMPLATE" | grep 'version:' | sed 's/.*version: *"\(.*\)"/\1/')"

if [[ -z "$CURRENT_VERSION" ]]; then
    echo "Error: could not read current version from $TEMPLATE"
    exit 1
fi

if [[ "$CURRENT_VERSION" == "$NEW_VERSION" ]]; then
    echo "Version is already $NEW_VERSION, nothing to do."
    exit 0
fi

echo "Bumping version: $CURRENT_VERSION -> $NEW_VERSION"

# 1. Update the single source of truth (template).
sed -i '' "s/version: \"$CURRENT_VERSION\"/version: \"$NEW_VERSION\"/" "$TEMPLATE"
echo "  Updated kss.module-template.yaml"

# 2. Update documentation references.
for file in README.md .ai/guidelines.md; do
    filepath="$PROJECT_DIR/$file"
    if [[ -f "$filepath" ]]; then
        sed -i '' "s/$CURRENT_VERSION/$NEW_VERSION/g" "$filepath"
        echo "  Updated $file"
    fi
done

echo ""
echo "Done. Version is now $NEW_VERSION."
echo "Verify with: grep -r '$NEW_VERSION' kss.module-template.yaml README.md .ai/guidelines.md"
