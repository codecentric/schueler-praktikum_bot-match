#!/bin/bash
# Erzeugt docs/backlog.pdf aus docs/backlog.md.
# Aufruf: docs/pdf/build.sh
set -euo pipefail
cd "$(dirname "$0")/../.."

TMP=$(mktemp -t backlog-pdf).md
trap 'rm -f "$TMP"' EXIT
sed 's/→/->/g' docs/backlog.md > "$TMP"

pandoc "$TMP" \
  -o docs/backlog.pdf \
  --pdf-engine=xelatex \
  -V documentclass=extarticle \
  -V geometry:"a4paper,margin=1.5cm" \
  -V mainfont="Helvetica Neue" \
  -V fontsize=14pt \
  -V colorlinks=true \
  --include-in-header=docs/pdf/header.tex \
  --lua-filter=docs/pdf/storybox.lua \
  --toc --toc-depth=2

echo "docs/backlog.pdf erzeugt."
