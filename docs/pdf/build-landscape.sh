#!/bin/bash
# Erzeugt docs/backlog-landscape.pdf: 1 Story pro Seite, Querformat, große Schrift.
# Aufruf: docs/pdf/build-landscape.sh
set -euo pipefail
cd "$(dirname "$0")/../.."

TMP=$(mktemp -t backlog-pdf).md
trap 'rm -f "$TMP"' EXIT
sed 's/→/->/g' docs/backlog.md > "$TMP"

pandoc "$TMP" \
  -o docs/backlog-landscape.pdf \
  --pdf-engine=xelatex \
  -V documentclass=extarticle \
  -V geometry:"a4paper,landscape,margin=1.5cm" \
  -V mainfont="Helvetica Neue" \
  -V fontsize=14pt \
  --include-in-header=docs/pdf/header-landscape.tex \
  --lua-filter=docs/pdf/storybox-landscape.lua

echo "docs/backlog-landscape.pdf erzeugt."
