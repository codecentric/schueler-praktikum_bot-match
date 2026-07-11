#!/bin/bash
# Erzeugt fuer jede docs/**/*.md (ausser backlog.md, siehe build.sh/build-landscape.sh)
# ein gleichnamiges PDF unter docs/pdf/, inkl. Unterordnern.
# Aufruf: docs/pdf/build/build-docs.sh
set -euo pipefail
cd "$(dirname "$0")/../../.."

while IFS= read -r -d '' src; do
  rel="${src#docs/}"
  [ "$rel" = "backlog.md" ] && continue
  case "$rel" in pdf/*) continue ;; esac

  out="docs/pdf/${rel%.md}.pdf"
  mkdir -p "$(dirname "$out")"

  TMP=$(mktemp -t docs-pdf).md
  trap 'rm -f "$TMP"' EXIT
  sed 's/→/->/g' "$src" > "$TMP"

  pandoc "$TMP" \
    -o "$out" \
    --resource-path="$(dirname "$src")" \
    --lua-filter=docs/pdf/build/html-img.lua \
    --pdf-engine=xelatex \
    -V geometry:"a4paper,margin=2cm" \
    -V mainfont="Helvetica Neue" \
    -V monofont="Menlo" \
    -V fontsize=11pt \
    -V colorlinks=true \
    --include-in-header=docs/pdf/build/header-docs.tex \
    --toc --toc-depth=2

  rm -f "$TMP"
  trap - EXIT
  echo "$out erzeugt."
done < <(find docs -name '*.md' -print0)
